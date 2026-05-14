package com.paramichha.testkit.generator;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.MethodMeta;
import com.paramichha.testkit.model.ParameterMeta;
import com.paramichha.testkit.utils.TestNames;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes one {ClassName}Method{MethodName}Test nested class per public method.
 *
 * <p>Value generation rules:
 * - Valid input  → DataFactory.of(T).valid()       → method must not throw
 * - Invalid input → DataFactory.of(T).invalidList() → method must throw
 *
 * <p>Return assertion rules — single unified path via wrapper + validator:
 * - void                              → doesNotThrow only
 * - primitive/String with annotations → inline wrapper record + validator.validate()
 * - Object with constrained fields    → validator.validate(result)
 * - Object/primitive — no annotations → isNotNull / doesNotThrow
 *
 * <p>All test method names start with "test".
 */
final class MethodTestWriter {

    private final ClassMeta meta;
    private final InstantiationWriter instantiation;
    private final TestKitTemplate template;

    MethodTestWriter(ClassMeta meta, InstantiationWriter instantiation,
                     TestKitTemplate template) {
        this.meta          = meta;
        this.instantiation = instantiation;
        this.template      = template;
    }

    void write(StringBuilder sb, List<Object> validInstanceParts, boolean hasJakarta) {
        for (MethodMeta method : meta.methods()) {
            writeMethodClass(sb, method, hasJakarta, true);
        }
    }

    void writeFlat(StringBuilder sb, MethodMeta method,
                   List<Object> validInstanceParts, boolean hasJakarta) {
        writeMethodClass(sb, method, hasJakarta, false);
    }

    // ── Core ──────────────────────────────────────────────────────────────

    private void writeMethodClass(StringBuilder sb, MethodMeta method,
                                  boolean hasJakarta, boolean nested) {
        String className   = meta.simpleName();
        String fieldName   = meta.utilityClass() ? className : TestNames.toVariable(className);
        String methodName  = method.name();
        String ind         = nested ? "        " : "    ";
        String indBody     = nested ? "            " : "        ";

        String nestedClass   = template.resolveMethod(template.methodClassName,    className, methodName);
        String nestedDisplay = template.resolveMethod(template.methodDisplay,       className, methodName);
        String validSrc      = template.resolveMethod(template.validInputsSource,   className, methodName);
        String invalidSrc    = template.resolveMethod(template.invalidInputsSource, className, methodName);

        sb.append("    // ").append("=".repeat(73)).append("\n");
        sb.append("    // ").append(nestedDisplay).append("\n");
        sb.append("    // ").append("=".repeat(73)).append("\n\n");

        if (nested) {
            sb.append("    @Nested\n");
            sb.append("    @DisplayName(\"").append(nestedDisplay).append("\")\n");
            sb.append("    class ").append(nestedClass).append(" {\n\n");
        }

        // Write wrapper record inside nested class if needed
        if (needsWrapperRecord(method)) {
            writeWrapperRecord(sb, method, ind);
        }

        switch (method.kind()) {
            case COMMAND     -> writeCommand(sb, method, fieldName, hasJakarta, ind, indBody);
            case QUERY       -> writeQuery(sb, method, fieldName, ind, indBody);
            case MUTATOR,
                 TRANSFORMER -> writeMutatorOrTransformer(sb, method, fieldName,
                    validSrc, invalidSrc, ind, indBody);
            case OPERATION   -> writeOperation(sb, method, fieldName,
                    validSrc, invalidSrc, ind, indBody);
        }

        if (nested) sb.append("    }\n\n");
    }

    // ── Wrapper record — boxes primitive/String for validation ────────────

    /**
     * Returns true when the return type needs a wrapper record:
     * primitive or String with Jakarta return annotations.
     */
    private boolean needsWrapperRecord(MethodMeta method) {
        if (method.returnsVoid()) return false;
        if (!method.hasReturnConstraints()) return false;
        Class<?> rt = method.returnType();
        return rt.isPrimitive() || rt == String.class || rt == Character.class;
    }

    /**
     * Writes a private inline record that boxes the primitive/String
     * with the same annotations as the method return — allowing
     * standard validator.validate() to work.
     *
     * Example:
     *   private record GetQuantityResult(@Min(1) @Max(100) Integer value) {}
     */
    private void writeWrapperRecord(StringBuilder sb, MethodMeta method, String ind) {
        String wrapperName  = wrapperName(method.name());
        String boxedType    = boxedTypeName(method.returnType());
        String annotations  = buildAnnotationSource(method.returnAnnotations());

        sb.append(ind).append("private record ").append(wrapperName).append("(")
                .append(annotations).append(boxedType).append(" value) {}\n\n");
    }

    private String wrapperName(String methodName) {
        return Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1) + "Result";
    }

    private String boxedTypeName(Class<?> type) {
        if (!type.isPrimitive()) return FieldTestWriter.sourceTypeName(type);
        return switch (type.getName()) {
            case "boolean" -> "Boolean";
            case "byte"    -> "Byte";
            case "short"   -> "Short";
            case "int"     -> "Integer";
            case "long"    -> "Long";
            case "float"   -> "Float";
            case "double"  -> "Double";
            case "char"    -> "Character";
            default        -> "Object";
        };
    }

    /**
     * Converts a list of annotation instances to their source representation.
     * e.g. [@Min(value=1), @Max(value=100)] → "@Min(1) @Max(100) "
     */
    private String buildAnnotationSource(List<Annotation> annotations) {
        if (annotations.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Annotation a : annotations) {
            sb.append(annotationToSource(a)).append(" ");
        }
        return sb.toString();
    }

    private String annotationToSource(Annotation a) {
        String simpleName = a.annotationType().getSimpleName();
        Method[] methods  = a.annotationType().getDeclaredMethods();

        if (methods.length == 0) return "@" + simpleName;

        // Single attribute named "value" — use shorthand: @Min(1)
        if (methods.length == 1 && methods[0].getName().equals("value")) {
            try {
                Object val = methods[0].invoke(a);
                return "@" + simpleName + "(" + attributeValueToSource(val) + ")";
            } catch (Exception ignored) {}
        }

        // Multiple attributes or non-"value" name — use named form: @Size(min=2, max=50)
        StringBuilder sb = new StringBuilder("@").append(simpleName).append("(");
        boolean first = true;
        for (Method m : methods) {
            try {
                Object val = m.invoke(a);
                Object def = m.getDefaultValue();
                // Skip default values to keep annotations clean
                if (def != null && def.equals(val)) continue;
                if (!first) sb.append(", ");
                sb.append(m.getName()).append("=").append(attributeValueToSource(val));
                first = false;
            } catch (Exception ignored) {}
        }
        sb.append(")");
        // If all were defaults, use no-arg form
        return sb.toString().equals("@" + simpleName + "()") ? "@" + simpleName : sb.toString();
    }

    private String attributeValueToSource(Object val) {
        if (val instanceof String s) return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        if (val instanceof Long l)   return l + "L";
        if (val instanceof Class<?> c) return c.getSimpleName() + ".class";
        if (val instanceof Annotation ann) return annotationToSource(ann);
        if (val != null && val.getClass().isArray()) {
            // e.g. groups() = {} — skip
            return "{}";
        }
        return String.valueOf(val);
    }

    // ── COMMAND ───────────────────────────────────────────────────────────

    private void writeCommand(StringBuilder sb, MethodMeta method, String fieldName,
                              boolean hasJakarta, String ind, String indBody) {
        String methodName = method.name();

        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"executes without exception\")\n");
        sb.append(ind).append("void ").append(template.testExecutesWithoutException).append("() {\n");
        sb.append(indBody).append("assertThatCode(() -> ")
                .append(fieldName).append(".").append(methodName).append("()).doesNotThrowAnyException();\n");
        sb.append(ind).append("}\n\n");

        if (hasJakarta) {
            sb.append(ind).append("@Test\n");
            sb.append(ind).append("@DisplayName(\"object remains valid after execution\")\n");
            sb.append(ind).append("void ").append(template.testObjectRemainsValid).append("() {\n");
            sb.append(indBody).append(fieldName).append(".").append(methodName).append("();\n");
            sb.append(indBody).append("assertThat(").append(template.validatorField)
                    .append(".validate(").append(fieldName).append(")).isEmpty();\n");
            sb.append(ind).append("}\n\n");
        }
    }

    // ── QUERY ─────────────────────────────────────────────────────────────

    private void writeQuery(StringBuilder sb, MethodMeta method, String fieldName,
                            String ind, String indBody) {
        String methodName = method.name();
        String call       = fieldName + "." + methodName + "()";

        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" returns a result\")\n");
        sb.append(ind).append("void ").append(template.testReturnsResult).append("() {\n");

        if (method.returnsVoid()) {
            sb.append(indBody).append("assertThatCode(() -> ").append(call).append(").doesNotThrowAnyException();\n");
        } else {
            String typeName = FieldTestWriter.sourceTypeName(method.returnType());
            sb.append(indBody).append(typeName).append(" result = ").append(call).append(";\n");
            writeReturnAssertion(sb, method, "result", indBody);
        }
        sb.append(ind).append("}\n\n");

        if (!method.returnsVoid()) {
            sb.append(ind).append("@Test\n");
            sb.append(ind).append("@DisplayName(\"").append(methodName).append(" returns consistent result\")\n");
            sb.append(ind).append("void ").append(template.testReturnsConsistentResult).append("() {\n");
            sb.append(indBody).append("assertThat(").append(call).append(")")
                    .append(".isEqualTo(").append(call).append(");\n");
            sb.append(ind).append("}\n\n");
        }
    }

    // ── MUTATOR / TRANSFORMER ─────────────────────────────────────────────

    private void writeMutatorOrTransformer(StringBuilder sb, MethodMeta method,
                                           String fieldName,
                                           String validSrc, String invalidSrc,
                                           String ind, String indBody) {
        String methodName      = method.name();
        List<ParameterMeta> params = method.parameters();
        String paramSignature  = buildParamSignature(params);
        String paramCall       = buildParamCall(params);
        boolean anyConstrained = params.stream().anyMatch(ParameterMeta::isConstrained);

        // testAcceptsValidInput — always
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(validSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" accepts valid input\")\n");
        sb.append(ind).append("void ").append(template.testAcceptsValidInput)
                .append("(String description, ").append(paramSignature).append(") {\n");
        writeAcceptBody(sb, method, fieldName, paramCall, indBody);
        sb.append(ind).append("}\n\n");

        // testRejectsInvalidInput — only if any param constrained
        if (anyConstrained) {
            sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
            sb.append(ind).append("@MethodSource(\"").append(invalidSrc).append("\")\n");
            sb.append(ind).append("@DisplayName(\"").append(methodName).append(" rejects invalid input\")\n");
            sb.append(ind).append("void ").append(template.testRejectsInvalidInput)
                    .append("(String description, ").append(paramSignature).append(") {\n");
            sb.append(indBody).append("assertThatThrownBy(() -> ")
                    .append(fieldName).append(".").append(methodName).append("(").append(paramCall).append("))\n");
            sb.append(indBody).append("        .isInstanceOf(Exception.class);\n");
            sb.append(ind).append("}\n\n");
        }

        writeValidDataSource(sb, method, params, validSrc, ind);
        if (anyConstrained) writeInvalidDataSource(sb, params, invalidSrc, ind);
    }

    // ── OPERATION ─────────────────────────────────────────────────────────

    private void writeOperation(StringBuilder sb, MethodMeta method,
                                String fieldName,
                                String validSrc, String invalidSrc,
                                String ind, String indBody) {
        String methodName      = method.name();
        List<ParameterMeta> params = method.parameters();
        String paramSignature  = buildParamSignature(params);
        String paramCall       = buildParamCall(params);
        boolean anyConstrained = params.stream().anyMatch(ParameterMeta::isConstrained);

        // testAcceptsValidInput — always
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(validSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" accepts valid input\")\n");
        sb.append(ind).append("void ").append(template.testAcceptsValidInput)
                .append("(String description, ").append(paramSignature).append(") {\n");
        writeAcceptBody(sb, method, fieldName, paramCall, indBody);
        sb.append(ind).append("}\n\n");

        // testRejectsInvalidInput — only if any param constrained
        if (anyConstrained) {
            sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
            sb.append(ind).append("@MethodSource(\"").append(invalidSrc).append("\")\n");
            sb.append(ind).append("@DisplayName(\"").append(methodName).append(" rejects invalid input\")\n");
            sb.append(ind).append("void ").append(template.testRejectsInvalidInput)
                    .append("(String description, ").append(paramSignature).append(") {\n");
            sb.append(indBody).append("assertThatThrownBy(() -> ")
                    .append(fieldName).append(".").append(methodName).append("(").append(paramCall).append("))\n");
            sb.append(indBody).append("        .isInstanceOf(Exception.class);\n");
            sb.append(ind).append("}\n\n");
        }

        writeValidDataSource(sb, method, params, validSrc, ind);
        if (anyConstrained) writeInvalidDataSourceOperation(sb, params, invalidSrc, ind);
    }

    // ── Accept body — shared by MUTATOR/TRANSFORMER/OPERATION ────────────

    private void writeAcceptBody(StringBuilder sb, MethodMeta method,
                                 String fieldName, String paramCall, String indBody) {
        String methodName = method.name();

        if (method.returnsVoid()) {
            sb.append(indBody).append("assertThatCode(() -> ")
                    .append(fieldName).append(".").append(methodName).append("(").append(paramCall)
                    .append(")).doesNotThrowAnyException();\n");
        } else {
            String typeName = FieldTestWriter.sourceTypeName(method.returnType());
            sb.append(indBody).append(typeName).append(" result = ")
                    .append(fieldName).append(".").append(methodName).append("(").append(paramCall).append(");\n");
            writeReturnAssertion(sb, method, "result", indBody);
        }
    }

    // ── Return assertion — single unified path ────────────────────────────

    /**
     * Single method that handles all return type + annotation permutations.
     *
     * primitive/String with annotations → wrapper record + validator.validate()
     * Object with constrained fields    → validator.validate(result)
     * Object no constraints             → assertThat(result).isNotNull()
     * primitive no annotations          → .doesNotThrowAnyException() (already handled at call site)
     */
    private void writeReturnAssertion(StringBuilder sb, MethodMeta method,
                                      String resultExpr, String indBody) {
        Class<?> returnType = method.returnType();

        if (returnType == void.class) return;

        // Annotated primitive or String — use wrapper record + validator
        if (needsWrapperRecord(method)) {
            sb.append(indBody).append("assertThat(").append(template.validatorField)
                    .append(".validate(new ").append(wrapperName(method.name()))
                    .append("(").append(resultExpr).append("))).isEmpty();\n");
            return;
        }

        // Object return with Jakarta return annotation (e.g. @Valid, @NotNull)
        if (!returnType.isPrimitive() && method.hasReturnConstraints()) {
            sb.append(indBody).append("assertThat(").append(template.validatorField)
                    .append(".validate(").append(resultExpr).append(")).isEmpty();\n");
            return;
        }

        // Object whose fields have constraints
        if (!returnType.isPrimitive() && typeHasConstraints(returnType)) {
            sb.append(indBody).append("assertThat(").append(template.validatorField)
                    .append(".validate(").append(resultExpr).append(")).isEmpty();\n");
            return;
        }

        // Plain primitive — .doesNotThrowAnyException() was already written at call site
        if (returnType.isPrimitive()) return;

        // Plain object — at minimum not null
        sb.append(indBody).append("assertThat(").append(resultExpr).append(").isNotNull();\n");
    }

    // ── Data sources ──────────────────────────────────────────────────────

    private void writeValidDataSource(StringBuilder sb, MethodMeta method,
                                      List<ParameterMeta> params,
                                      String validSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(validSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");
        sb.append(ind).append("        Arguments.of(\"valid ").append(method.name()).append("\",\n");
        for (int i = 0; i < params.size(); i++) {
            sb.append(ind).append("            ").append(validLiteralFor(params.get(i)));
            sb.append(i < params.size() - 1 ? ",\n" : ")\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    private void writeInvalidDataSource(StringBuilder sb,
                                        List<ParameterMeta> params,
                                        String invalidSrc, String ind) {
        ParameterMeta p = params.get(0);
        List<String> invalids = invalidLiteralsFor(p);

        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");
        for (int i = 0; i < invalids.size(); i++) {
            sb.append(ind).append("        Arguments.of(\"invalid ").append(p.name())
                    .append(" ").append(i + 1).append("\", ").append(invalids.get(i)).append(")");
            sb.append(i < invalids.size() - 1 ? ",\n" : "\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    private void writeInvalidDataSourceOperation(StringBuilder sb,
                                                 List<ParameterMeta> params,
                                                 String invalidSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");

        List<String> rows = new ArrayList<>();
        for (int nullIdx = 0; nullIdx < params.size(); nullIdx++) {
            ParameterMeta nullParam = params.get(nullIdx);
            if (!nullParam.isConstrained()) continue;

            StringBuilder row = new StringBuilder();
            row.append(ind).append("        Arguments.of(\"null ").append(nullParam.name()).append("\",\n");
            for (int i = 0; i < params.size(); i++) {
                String val = i == nullIdx ? nullCast(params.get(i).type())
                        : validLiteralFor(params.get(i));
                row.append(ind).append("            ").append(val);
                row.append(i < params.size() - 1 ? ",\n" : ")");
            }
            rows.add(row.toString());
        }
        for (int i = 0; i < rows.size(); i++) {
            sb.append(rows.get(i));
            sb.append(i < rows.size() - 1 ? ",\n" : "\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    // ── Value generation ──────────────────────────────────────────────────

    private String validLiteralFor(ParameterMeta param) {
        Class<?> type = param.type();
        if (type.isPrimitive()) return primitiveValid(type);
        try {
            Object value = DataFactory.of(type).seed(param.name().hashCode()).valid();
            String literal = LiteralConverter.toLiteralOrNull(value);
            if (literal != null) return literal;
        } catch (Exception ignored) {}
        return nullCast(type);
    }

    private List<String> invalidLiteralsFor(ParameterMeta param) {
        Class<?> type = param.type();
        List<String> result = new ArrayList<>();

        if (type == String.class) {
            result.add("(String) null");
            result.add("\"\"");
            result.add("\"   \"");
            return result;
        }

        // Primitive — boundary values from DataFactory invalidList on wrapper
        if (type.isPrimitive()) {
            try {
                Class<?> boxed = boxedClass(type);
                List<?> invalids = DataFactory.of(boxed).invalidList();
                for (Object inv : invalids) {
                    String lit = LiteralConverter.toLiteralOrNull(inv);
                    if (lit != null) result.add(lit);
                    if (result.size() >= 3) break;
                }
            } catch (Exception ignored) {}
            return result;
        }

        // Object with indirect constraints — DataFactory.invalidList()
        if (param.hasIndirectConstraints()) {
            try {
                List<?> invalids = DataFactory.of(type).invalidList();
                for (Object inv : invalids) {
                    String lit = LiteralConverter.toLiteralOrNull(inv);
                    if (lit != null) result.add(lit);
                    if (result.size() >= 3) break;
                }
            } catch (Exception ignored) {}
        }

        if (result.isEmpty()) result.add(nullCast(type));
        return result;
    }

    // ── Type helpers ──────────────────────────────────────────────────────

    private boolean typeHasConstraints(Class<?> type) {
        if (type == null || type.isPrimitive()) return false;
        if (type.getPackageName().startsWith("java.")) return false;
        if (type.isEnum()) return false;
        for (java.lang.reflect.Field f : type.getDeclaredFields()) {
            for (Annotation a : f.getAnnotations()) {
                String pkg = a.annotationType().getPackageName();
                if (pkg.startsWith("jakarta.validation") || pkg.startsWith("javax.validation")
                        || pkg.startsWith("org.hibernate.validator")) return true;
            }
        }
        if (type.isRecord()) {
            for (java.lang.reflect.RecordComponent rc : type.getRecordComponents()) {
                for (Annotation a : rc.getAnnotations()) {
                    String pkg = a.annotationType().getPackageName();
                    if (pkg.startsWith("jakarta.validation") || pkg.startsWith("javax.validation")
                            || pkg.startsWith("org.hibernate.validator")) return true;
                }
            }
        }
        return false;
    }

    private Class<?> boxedClass(Class<?> primitive) {
        return switch (primitive.getName()) {
            case "boolean" -> Boolean.class;
            case "byte"    -> Byte.class;
            case "short"   -> Short.class;
            case "int"     -> Integer.class;
            case "long"    -> Long.class;
            case "float"   -> Float.class;
            case "double"  -> Double.class;
            case "char"    -> Character.class;
            default        -> primitive;
        };
    }

    // ── String helpers ────────────────────────────────────────────────────

    private String buildParamSignature(List<ParameterMeta> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(FieldTestWriter.sourceTypeName(params.get(i).type()))
                    .append(" ").append(params.get(i).name());
        }
        return sb.toString();
    }

    private String buildParamCall(List<ParameterMeta> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).name());
        }
        return sb.toString();
    }

    private String nullCast(Class<?> type) {
        if (type.isPrimitive()) return primitiveValid(type);
        return "(" + FieldTestWriter.sourceTypeName(type) + ") null";
    }

    private String primitiveValid(Class<?> type) {
        return switch (type.getName()) {
            case "boolean" -> "true";
            case "byte"    -> "(byte) 1";
            case "short"   -> "(short) 1";
            case "int"     -> "1";
            case "long"    -> "1L";
            case "float"   -> "1.0f";
            case "double"  -> "1.0d";
            case "char"    -> "'a'";
            default        -> "0";
        };
    }
}