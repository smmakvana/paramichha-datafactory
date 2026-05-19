package com.paramichha.testkit.generator;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.testkit.ReflectionClassReader;
import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.*;
import com.paramichha.testkit.utils.TestNames;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Writes one nested test class per public method.
 *
 * Pure iteration — no value generation logic.
 * DataFactory drives all values via type + annotations.
 * LiteralConverter converts objects to Java source strings.
 */
final class MethodTestWriter {

    private final ReflectionClassInfo meta;
    private final InstantiationWriter instantiation;
    private final TestKitTemplate template;

    MethodTestWriter(ReflectionClassInfo meta, InstantiationWriter instantiation,
                     TestKitTemplate template) {
        this.meta          = meta;
        this.instantiation = instantiation;
        this.template      = template;
    }

    void write(StringBuilder sb, List<Object> validInstanceParts, boolean hasJakarta) {
        for (ReflectionMethodInfo method : meta.methods()) {
            writeMethodClass(sb, method, hasJakarta, true);
        }
    }

    void writeFlat(StringBuilder sb, ReflectionMethodInfo method,
                   List<Object> validInstanceParts, boolean hasJakarta) {
        writeMethodClass(sb, method, hasJakarta, false);
    }

    // ── Core ──────────────────────────────────────────────────────────────

    private void writeMethodClass(StringBuilder sb, ReflectionMethodInfo method,
                                  boolean hasJakarta, boolean nested) {
        String className  = meta.simpleName();
        String fieldName  = meta.utilityClass() ? className : TestNames.toVariable(className);
        String methodName = method.name();
        String ind        = nested ? "        " : "    ";
        String indBody    = nested ? "            " : "        ";

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

        // Wrapper record — only if return type needs boxing for validator
        if (needsWrapperRecord(method.returnType())) {
            writeWrapperRecord(sb, methodName, method.returnType(), ind);
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

    // ── COMMAND ───────────────────────────────────────────────────────────

    private void writeCommand(StringBuilder sb, ReflectionMethodInfo method, String fieldName,
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

    private void writeQuery(StringBuilder sb, ReflectionMethodInfo method, String fieldName,
                            String ind, String indBody) {
        String methodName   = method.name();
        ReflectionReturnTypeInfo ret  = method.returnType();
        String call         = fieldName + "." + methodName + "()";

        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" returns a result\")\n");
        sb.append(ind).append("void ").append(template.testReturnsResult).append("() {\n");

        if (ret.isVoid()) {
            sb.append(indBody).append("assertThatCode(() -> ").append(call).append(").doesNotThrowAnyException();\n");
        } else {
            String typeName = FieldTestWriter.sourceTypeName(ret.type());
            sb.append(indBody).append(typeName).append(" result = ").append(call).append(";\n");
            writeReturnAssertion(sb, ret, methodName, "result", indBody);
        }
        sb.append(ind).append("}\n\n");

        if (!ret.isVoid()) {
            sb.append(ind).append("@Test\n");
            sb.append(ind).append("@DisplayName(\"").append(methodName).append(" returns consistent result\")\n");
            sb.append(ind).append("void ").append(template.testReturnsConsistentResult).append("() {\n");
            sb.append(indBody).append("assertThat(").append(call).append(")")
                    .append(".isEqualTo(").append(call).append(");\n");
            sb.append(ind).append("}\n\n");
        }
    }

    // ── MUTATOR / TRANSFORMER ─────────────────────────────────────────────

    private void writeMutatorOrTransformer(StringBuilder sb, ReflectionMethodInfo method,
                                           String fieldName,
                                           String validSrc, String invalidSrc,
                                           String ind, String indBody) {
        String methodName      = method.name();
        List<ParameterInfo> params = method.parameters();
        String sig             = buildParamSignature(params);
        String call            = buildParamCall(params);
        boolean anyConstrained = params.stream().anyMatch(ParameterInfo::isConstrained);

        writeAcceptsTest(sb, method, fieldName, validSrc, sig, call, ind, indBody);

        if (anyConstrained) {
            writeRejectsTest(sb, methodName, fieldName, invalidSrc, sig, call, params, ind, indBody);
        }

        writeValidDataSource(sb, methodName, params, validSrc, ind);
        if (anyConstrained) writeInvalidDataSource(sb, params, invalidSrc, ind);
    }

    // ── OPERATION ─────────────────────────────────────────────────────────

    private void writeOperation(StringBuilder sb, ReflectionMethodInfo method,
                                String fieldName,
                                String validSrc, String invalidSrc,
                                String ind, String indBody) {
        String methodName      = method.name();
        List<ParameterInfo> params = method.parameters();
        String sig             = buildParamSignature(params);
        String call            = buildParamCall(params);
        boolean anyConstrained = params.stream().anyMatch(ParameterInfo::isConstrained);

        writeAcceptsTest(sb, method, fieldName, validSrc, sig, call, ind, indBody);

        if (anyConstrained) {
            writeRejectsTest(sb, methodName, fieldName, invalidSrc, sig, call, params, ind, indBody);
        }

        writeValidDataSource(sb, methodName, params, validSrc, ind);
        if (anyConstrained) writeInvalidDataSourceOperation(sb, params, invalidSrc, ind);
    }

    // ── Shared test methods ───────────────────────────────────────────────

    private void writeAcceptsTest(StringBuilder sb, ReflectionMethodInfo method, String fieldName,
                                  String validSrc, String sig, String call,
                                  String ind, String indBody) {
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(validSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(method.name()).append(" accepts valid input\")\n");
        sb.append(ind).append("void ").append(template.testAcceptsValidInput)
                .append("(String description, ").append(sig).append(") {\n");

        ReflectionReturnTypeInfo ret = method.returnType();
        if (ret.isVoid()) {
            sb.append(indBody).append("assertThatCode(() -> ")
                    .append(fieldName).append(".").append(method.name()).append("(").append(call)
                    .append(")).doesNotThrowAnyException();\n");
        } else {
            String typeName = FieldTestWriter.sourceTypeName(ret.type());
            sb.append(indBody).append(typeName).append(" result = ")
                    .append(fieldName).append(".").append(method.name()).append("(").append(call).append(");\n");
            writeReturnAssertion(sb, ret, method.name(), "result", indBody);
        }
        sb.append(ind).append("}\n\n");
    }

    private void writeRejectsTest(StringBuilder sb, String methodName, String fieldName,
                                  String invalidSrc, String sig, String call,
                                  List<ParameterInfo> params, String ind, String indBody) {
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(invalidSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" rejects invalid input\")\n");
        sb.append(ind).append("void ").append(template.testRejectsInvalidInput)
                .append("(String description, ").append(sig).append(") {\n");
        writeEnforcementHints(sb, params, indBody);
        sb.append(indBody).append("assertThatThrownBy(() -> ")
                .append(fieldName).append(".").append(methodName).append("(").append(call).append("))\n");
        sb.append(indBody).append("        .isInstanceOf(Exception.class);\n");
        sb.append(ind).append("}\n\n");
    }

    /**
     * Writes a comment per constrained param showing the DataValidation call
     * that enforces the contract — teaches dev exactly what to add to make test pass.
     */
    private void writeEnforcementHints(StringBuilder sb, List<ParameterInfo> params, String indBody) {
        for (ParameterInfo param : params) {
            for (java.lang.annotation.Annotation a : param.validations()) {
                String hint = dataValidationCall(a, param.name());
                if (hint != null) {
                    sb.append(indBody).append("// Enforce @")
                            .append(a.annotationType().getSimpleName())
                            .append(": DataValidation.").append(hint).append(";\n");
                }
            }
        }
    }

    // ── Return assertion ──────────────────────────────────────────────────

    /**
     * Writes the return assertion — driven entirely by ReturnTypeMeta.
     *
     * Rules (universal — Spring or not):
     * - void / no annotations → doesNotThrow already covers it — nothing added
     * - @Valid                → validator.validate(result).isEmpty() — deep field validation
     * - other annotations    → DataValidation.xxx(result) — one call per annotation
     *                          works for all types: primitive, String, BigDecimal, Object
     * - no annotations       → skip for primitive, isNotNull for object
     */
    private void writeReturnAssertion(StringBuilder sb, ReflectionReturnTypeInfo ret,
                                      String methodName, String resultExpr, String indBody) {
        if (ret.isVoid()) return;

        if (ret.isConstrained()) {
            for (java.lang.annotation.Annotation a : ret.annotations()) {
                String ann = a.annotationType().getSimpleName();
                if (ann.equals("Valid")) {
                    // @Valid — deep field validation via Jakarta validator
                    sb.append(indBody).append("assertThat(").append(template.validatorField)
                            .append(".validate(").append(resultExpr).append(")).isEmpty();\n");
                } else {
                    // All other annotations — DataValidation mirrors the contract
                    String call = dataValidationCall(a, resultExpr);
                    if (call != null) {
                        sb.append(indBody).append("DataValidation.").append(call).append(";\n");
                    }
                }
            }
            return;
        }

        if (typeHasConstraints(ret.type())) {
            sb.append(indBody).append("assertThat(").append(template.validatorField)
                    .append(".validate(").append(resultExpr).append(")).isEmpty();\n");
            return;
        }

        if (ret.isPrimitive()) return; // any primitive value is valid — doesNotThrow covers it

        sb.append(indBody).append("assertThat(").append(resultExpr).append(").isNotNull();\n");
    }

    /**
     * Maps a Jakarta annotation to its DataValidation equivalent.
     * Returns null if no direct mapping (annotation is documentation only).
     */
    private String dataValidationCall(java.lang.annotation.Annotation a, String expr) {
        String name = a.annotationType().getSimpleName();
        try {
            return switch (name) {
                case "NotNull"        -> "notNull(" + expr + ")";
                case "NotBlank"       -> "notBlank(" + expr + ")";
                case "NotEmpty"       -> "notEmpty(" + expr + ")";
                case "Positive"       -> "positive(" + expr + ")";
                case "PositiveOrZero" -> "positiveOrZero(" + expr + ")";
                case "Negative"       -> "negative(" + expr + ")";
                case "NegativeOrZero" -> "negativeOrZero(" + expr + ")";
                case "Min"            -> "min(" + longAttr(a, "value") + ", " + expr + ")";
                case "Max"            -> "max(" + longAttr(a, "value") + ", " + expr + ")";
                case "DecimalMin"     -> "decimalMin(\"" + stringAttr(a, "value") + "\", " + expr + ")";
                case "DecimalMax"     -> "decimalMax(\"" + stringAttr(a, "value") + "\", " + expr + ")";
                case "Size"           -> "size(" + intAttr(a,"min") + ", " + intAttr(a,"max") + ", " + expr + ")";
                case "Pattern"        -> "pattern(\"" + stringAttr(a,"regexp") + "\", " + expr + ")";
                default               -> null;
            };
        } catch (Exception ignored) { return null; }
    }

    private long longAttr(java.lang.annotation.Annotation a, String attr) throws Exception {
        return (long) a.annotationType().getMethod(attr).invoke(a);
    }
    private String stringAttr(java.lang.annotation.Annotation a, String attr) throws Exception {
        return (String) a.annotationType().getMethod(attr).invoke(a);
    }
    private int intAttr(java.lang.annotation.Annotation a, String attr) throws Exception {
        return (int) a.annotationType().getMethod(attr).invoke(a);
    }

    // ── Wrapper record ────────────────────────────────────────────────────

    /**
     * Needs a wrapper when the return type can't be validated directly by
     * the Jakarta validator — primitives, String, and java.* types need boxing.
     */
    private boolean needsWrapperRecord(ReflectionReturnTypeInfo ret) {
        if (ret.isVoid() || !ret.isConstrained()) return false;
        Class<?> t = ret.type();
        return t.isPrimitive()
                || t == String.class
                || t == Character.class
                || t.getPackageName().startsWith("java.")
                || t.getPackageName().startsWith("javax.");
    }

    private void writeWrapperRecord(StringBuilder sb, String methodName,
                                    ReflectionReturnTypeInfo ret, String ind) {
        String annotations = buildAnnotationSource(ret.annotations());
        String boxedType   = boxedTypeName(ret.type());
        sb.append(ind).append("private record ").append(wrapperName(methodName))
                .append("(").append(annotations).append(boxedType).append(" value) {}\n\n");
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

    // ── Data sources — DataFactory drives all values ──────────────────────

    private void writeValidDataSource(StringBuilder sb, String methodName,
                                      List<ParameterInfo> params,
                                      String validSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(validSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");
        sb.append(ind).append("        Arguments.of(\"valid ").append(methodName).append("\",\n");
        for (int i = 0; i < params.size(); i++) {
            sb.append(ind).append("            ").append(validLiteral(params.get(i)));
            sb.append(i < params.size() - 1 ? ",\n" : ")\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    private void writeInvalidDataSource(StringBuilder sb, List<ParameterInfo> params,
                                        String invalidSrc, String ind) {
        ParameterInfo constrained = params.stream()
                .filter(ParameterInfo::isConstrained).findFirst().orElse(params.get(0));
        int constrainedIdx = params.indexOf(constrained);
        List<String> invalids = invalidLiterals(constrained);

        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");

        for (int i = 0; i < invalids.size(); i++) {
            if (params.size() == 1) {
                sb.append(ind).append("        Arguments.of(\"invalid ").append(constrained.name())
                        .append(" ").append(i + 1).append("\", ").append(invalids.get(i)).append(")");
            } else {
                sb.append(ind).append("        Arguments.of(\"invalid ").append(constrained.name())
                        .append(" ").append(i + 1).append("\",\n");
                for (int p = 0; p < params.size(); p++) {
                    String val = p == constrainedIdx ? invalids.get(i) : validLiteral(params.get(p));
                    sb.append(ind).append("            ").append(val);
                    sb.append(p < params.size() - 1 ? ",\n" : ")");
                }
            }
            sb.append(i < invalids.size() - 1 ? ",\n" : "\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    private void writeInvalidDataSourceOperation(StringBuilder sb, List<ParameterInfo> params,
                                                 String invalidSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");

        java.util.List<String> rows = new java.util.ArrayList<>();
        for (int nullIdx = 0; nullIdx < params.size(); nullIdx++) {
            ParameterInfo p = params.get(nullIdx);
            if (!p.isConstrained()) continue;
            List<String> invalids = invalidLiterals(p);
            for (String inv : invalids) {
                StringBuilder row = new StringBuilder();
                row.append(ind).append("        Arguments.of(\"invalid ").append(p.name()).append("\",\n");
                for (int i = 0; i < params.size(); i++) {
                    String val = i == nullIdx ? inv : validLiteral(params.get(i));
                    row.append(ind).append("            ").append(val);
                    row.append(i < params.size() - 1 ? ",\n" : ")");
                }
                rows.add(row.toString());
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            sb.append(rows.get(i));
            sb.append(i < rows.size() - 1 ? ",\n" : "\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    // ── Value generation — DataFactory only ──────────────────────────────

    @SuppressWarnings("unchecked")
    private String validLiteral(ParameterInfo param) {
        Class<?> type = param.type();

        // Domain object — build field by field using InstantiationWriter
        // Do this FIRST because DataFactory.of(DomainType) may fail if constructor
        // validates inputs and the generated seed produces an invalid combination
        String pkg = type.isPrimitive() ? "" : type.getPackageName();
        boolean isDomain = !type.isPrimitive() && !type.isEnum()
                && !pkg.startsWith("java.") && !pkg.startsWith("jakarta.")
                && !pkg.startsWith("javax.");
        if (isDomain) {
            try {
                ReflectionClassInfo paramMeta =
                        ReflectionClassReader.INSTANCE.read(type);
                InstantiationWriter iw = new InstantiationWriter(paramMeta);
                java.util.List<Object> parts = new java.util.ArrayList<>();
                for (ReflectionFieldInfo f : paramMeta.fields()) {
                    java.util.List<?> vals = iw.validValuesFor(type, f);
                    parts.add(vals.isEmpty() ? null : vals.get(0));
                }
                String call = iw.constructorCall(parts);
                if (call != null && !call.startsWith("/* TODO")) return call;
            } catch (Exception ignored) {}
            return nullCast(type);
        }

        // All other types — DataFactory direct (primitives, String, BigDecimal etc)
        long seed = com.paramichha.utils.Seeds.stableHash(param.name());
        try {
            Object val = param.validations().isEmpty()
                    ? DataFactory.of(type).seed(seed).valid()
                    : DataFactory.field(param.name(), (Class<Object>) type,
                    param.validations()).valid();
            if (val == null) return nullCast(type);
            String lit = LiteralConverter.toLiteralOrNull(val);
            if (lit != null && !lit.startsWith("/* TODO")) return lit;
        } catch (Exception ignored) {}
        return nullCast(type);
    }

    @SuppressWarnings("unchecked")
    private List<String> invalidLiterals(ParameterInfo param) {
        try {
            List<?> invalids = DataFactory.field(
                    param.name(), (Class<Object>) param.type(),
                    param.validations()).invalidList();
            java.util.List<String> result = new java.util.ArrayList<>();
            for (Object inv : invalids) {
                String lit = LiteralConverter.toLiteralOrNull(inv);
                if (lit != null) result.add(lit);
            }
            if (!result.isEmpty()) return result;
        } catch (Exception ignored) {}
        // Fallback — null is always invalid for object types
        if (!param.type().isPrimitive()) return java.util.List.of(nullCast(param.type()));
        return java.util.List.of();
    }

    // ── Annotation → source string ────────────────────────────────────────

    private String buildAnnotationSource(List<Annotation> annotations) {
        if (annotations.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Annotation a : annotations) sb.append(annotationToSource(a)).append(" ");
        return sb.toString();
    }

    private String annotationToSource(Annotation a) {
        String name    = a.annotationType().getSimpleName();
        Method[] attrs = a.annotationType().getDeclaredMethods();
        if (attrs.length == 0) return "@" + name;

        if (attrs.length == 1 && attrs[0].getName().equals("value")) {
            try {
                Object val = attrs[0].invoke(a);
                String src = attrToSource(val);
                if (src == null) return "@" + name;
                if (src.endsWith("L")) src = src.substring(0, src.length() - 1);
                return "@" + name + "(" + src + ")";
            } catch (Exception ignored) {}
        }

        StringBuilder sb = new StringBuilder("@").append(name).append("(");
        boolean first = true;
        for (Method m : attrs) {
            try {
                Object val = m.invoke(a);
                Object def = m.getDefaultValue();
                if (def != null && def.equals(val)) continue;
                String src = attrToSource(val);
                if (src == null) continue;
                if (!first) sb.append(", ");
                sb.append(m.getName()).append("=").append(src);
                first = false;
            } catch (Exception ignored) {}
        }
        sb.append(")");
        return sb.toString().equals("@" + name + "()") ? "@" + name : sb.toString();
    }

    private String attrToSource(Object val) {
        if (val instanceof String s)    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        if (val instanceof Long l)      return l + "L";
        if (val instanceof Class<?> c)  return c.getSimpleName() + ".class";
        if (val instanceof Annotation ann) return annotationToSource(ann);
        if (val != null && val.getClass().isArray()) return null;
        return val == null ? null : String.valueOf(val);
    }

    // ── Type helpers ──────────────────────────────────────────────────────

    private boolean typeHasConstraints(Class<?> type) {
        if (type == null || type.isPrimitive()) return false;
        if (type.getPackageName().startsWith("java.")) return false;
        if (type.isEnum()) return false;
        for (java.lang.reflect.Field f : type.getDeclaredFields())
            for (Annotation a : f.getAnnotations())
                if (isJakartaAnnotation(a)) return true;
        if (type.isRecord())
            for (java.lang.reflect.RecordComponent rc : type.getRecordComponents())
                for (Annotation a : rc.getAnnotations())
                    if (isJakartaAnnotation(a)) return true;
        return false;
    }

    private boolean isJakartaAnnotation(Annotation a) {
        String pkg = a.annotationType().getPackageName();
        return pkg.startsWith("jakarta.validation")
                || pkg.startsWith("javax.validation")
                || pkg.startsWith("org.hibernate.validator");
    }

    // ── String helpers ────────────────────────────────────────────────────

    private String buildParamSignature(List<ParameterInfo> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(FieldTestWriter.sourceTypeName(params.get(i).type()))
                    .append(" ").append(params.get(i).name());
        }
        return sb.toString();
    }

    private String buildParamCall(List<ParameterInfo> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).name());
        }
        return sb.toString();
    }

    private String nullCast(Class<?> type) {
        if (type.isPrimitive()) {
            // Primitive — return a valid fallback value (DataFactory failed)
            return switch (type.getName()) {
                case "boolean" -> "false";
                case "byte"    -> "(byte) 0";
                case "short"   -> "(short) 0";
                case "long"    -> "0L";
                case "float"   -> "0.0f";
                case "double"  -> "0.0d";
                case "char"    -> "'\\0'";
                default        -> "0";
            };
        }
        return "(" + FieldTestWriter.sourceTypeName(type) + ") null";
    }
}