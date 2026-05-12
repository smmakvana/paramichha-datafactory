package com.paramichha.testkit.generator;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.MethodKind;
import com.paramichha.testkit.model.MethodMeta;
import com.paramichha.testkit.model.ParameterMeta;
import com.paramichha.testkit.template.TemplateResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes one {ClassName}Method{MethodName}Test nested class per public method.
 *
 * <p>Generated tests are self-contained — no DataFactory dependency in output.
 * DataFactory is called here at generation time to produce hardcoded literals.
 *
 * <p>All test method names start with "test" per Java convention.
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

    /** Writes all method nested classes into sb. */
    void write(StringBuilder sb, List<Object> validInstanceParts, boolean hasJakarta) {
        for (MethodMeta method : meta.methods()) {
            writeMethodClass(sb, method, validInstanceParts, hasJakarta, true);
        }
    }

    /** Split mode — writes one method class without @Nested wrapper. */
    void writeFlat(StringBuilder sb, MethodMeta method,
                   List<Object> validInstanceParts, boolean hasJakarta) {
        writeMethodClass(sb, method, validInstanceParts, hasJakarta, false);
    }

    // ── Core ──────────────────────────────────────────────────────────────

    private void writeMethodClass(StringBuilder sb, MethodMeta method,
                                   List<Object> validInstanceParts,
                                   boolean hasJakarta, boolean nested) {

        String className   = meta.simpleName();
        String fieldName   = TemplateResolver.instanceFieldName(className);
        String methodName  = method.name();
        String ind         = nested ? "        " : "    ";
        String indBody     = nested ? "            " : "        ";

        String nestedClass   = template.resolveMethod(template.methodClassName,   className, methodName);
        String nestedDisplay = template.resolveMethod(template.methodDisplay,      className, methodName);
        String validSrc      = template.resolveMethod(template.validInputsSource,  className, methodName);
        String invalidSrc    = template.resolveMethod(template.invalidInputsSource, className, methodName);

        // ── Section header ─────────────────────────────────────────────
        sb.append("    // ").append("=".repeat(73)).append("\n");
        sb.append("    // ").append(nestedDisplay).append("\n");
        sb.append("    // ").append("=".repeat(73)).append("\n\n");

        if (nested) {
            sb.append("    @Nested\n");
            sb.append("    @DisplayName(\"").append(nestedDisplay).append("\")\n");
            sb.append("    class ").append(nestedClass).append(" {\n\n");
        }

        switch (method.kind()) {
            case COMMAND    -> writeCommand(sb, method, fieldName, hasJakarta, ind, indBody);
            case QUERY      -> writeQuery(sb, method, fieldName, ind, indBody);
            case MUTATOR    -> writeMutatorOrTransformer(sb, method, fieldName,
                    validInstanceParts, validSrc, invalidSrc, ind, indBody);
            case TRANSFORMER -> writeMutatorOrTransformer(sb, method, fieldName,
                    validInstanceParts, validSrc, invalidSrc, ind, indBody);
            case OPERATION  -> writeOperation(sb, method, fieldName,
                    validInstanceParts, validSrc, invalidSrc, ind, indBody);
        }

        if (nested) sb.append("    }\n\n");
    }

    // ── COMMAND ───────────────────────────────────────────────────────────

    private void writeCommand(StringBuilder sb, MethodMeta method, String fieldName,
                               boolean hasJakarta, String ind, String indBody) {
        String methodName = method.name();

        // testExecutesWithoutException
        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"executes without exception\")\n");
        sb.append(ind).append("void ").append(template.testExecutesWithoutException).append("() {\n");
        sb.append(indBody).append("assertThatCode(() -> ")
          .append(fieldName).append(".").append(methodName).append("()).doesNotThrow();\n");
        sb.append(ind).append("}\n\n");

        // testObjectRemainsValidAfterExecution — only if class has Jakarta annotations
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
        String call = fieldName + "." + methodName + "()";

        // testReturnsResult
        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" returns a result\")\n");
        sb.append(ind).append("void ").append(template.testReturnsResult).append("() {\n");

        if (method.returnsVoid()) {
            sb.append(indBody).append("assertThatCode(() -> ").append(call).append(").doesNotThrow();\n");
        } else if (method.returnsPrimitive()) {
            sb.append(indBody).append("var result = ").append(call).append(";\n");
            sb.append(indBody).append("// TODO: assertThat(result).isEqualTo(expectedValue);\n");
        } else {
            sb.append(indBody).append("assertThat(").append(call).append(").isNotNull();\n");
        }
        sb.append(ind).append("}\n\n");

        // testReturnsConsistentResult — skip for primitives with side effects
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
                                            String fieldName, List<Object> validParts,
                                            String validSrc, String invalidSrc,
                                            String ind, String indBody) {
        String methodName = method.name();
        List<ParameterMeta> params = method.parameters();
        String paramSignature = buildParamSignature(params);
        String paramCall      = buildParamCall(params);

        // testAcceptsValidInput
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(validSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" accepts valid input\")\n");
        sb.append(ind).append("void ").append(template.testAcceptsValidInput)
          .append("(String description, ").append(paramSignature).append(") {\n");

        if (method.returnsVoid()) {
            sb.append(indBody).append("assertThatCode(() -> ")
              .append(fieldName).append(".").append(methodName).append("(").append(paramCall)
              .append(")).doesNotThrow();\n");
        } else {
            String returnTypeName = FieldTestWriter.sourceTypeName(method.returnType());
            sb.append(indBody).append(returnTypeName).append(" result = ")
              .append(fieldName).append(".").append(methodName).append("(").append(paramCall).append(");\n");
            sb.append(indBody).append("assertThat(result).isNotNull();\n");
        }
        sb.append(ind).append("}\n\n");

        // testRejectsInvalidInput
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(invalidSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" rejects invalid input\")\n");
        sb.append(ind).append("void ").append(template.testRejectsInvalidInput)
          .append("(String description, ").append(paramSignature).append(") {\n");
        sb.append(indBody).append("assertThatThrownBy(() -> ")
          .append(fieldName).append(".").append(methodName).append("(").append(paramCall).append("))\n");
        sb.append(indBody).append("        .isInstanceOf(Exception.class);\n");
        sb.append(ind).append("}\n\n");

        // Data sources
        writeValidDataSource(sb, method, params, validParts, validSrc, ind);
        writeInvalidDataSource(sb, method, params, validParts, invalidSrc, ind);
    }

    // ── OPERATION ─────────────────────────────────────────────────────────

    private void writeOperation(StringBuilder sb, MethodMeta method,
                                 String fieldName, List<Object> validParts,
                                 String validSrc, String invalidSrc,
                                 String ind, String indBody) {
        String methodName = method.name();
        List<ParameterMeta> params = method.parameters();
        String paramSignature = buildParamSignature(params);
        String paramCall      = buildParamCall(params);

        // testAcceptsValidInput
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(validSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" accepts valid input\")\n");
        sb.append(ind).append("void ").append(template.testAcceptsValidInput)
          .append("(String description, ").append(paramSignature).append(") {\n");

        if (method.returnsVoid()) {
            sb.append(indBody).append("assertThatCode(() -> ")
              .append(fieldName).append(".").append(methodName).append("(").append(paramCall)
              .append(")).doesNotThrow();\n");
        } else {
            sb.append(indBody).append("assertThat(")
              .append(fieldName).append(".").append(methodName).append("(").append(paramCall)
              .append(")).isNotNull();\n");
        }
        sb.append(ind).append("}\n\n");

        // testRejectsInvalidInput — one row per parameter (null each one in turn)
        sb.append(ind).append("@ParameterizedTest(name = \"{0}\")\n");
        sb.append(ind).append("@MethodSource(\"").append(invalidSrc).append("\")\n");
        sb.append(ind).append("@DisplayName(\"").append(methodName).append(" rejects invalid input\")\n");
        sb.append(ind).append("void ").append(template.testRejectsInvalidInput)
          .append("(String description, ").append(paramSignature).append(") {\n");
        sb.append(indBody).append("assertThatThrownBy(() -> ")
          .append(fieldName).append(".").append(methodName).append("(").append(paramCall).append("))\n");
        sb.append(indBody).append("        .isInstanceOf(Exception.class);\n");
        sb.append(ind).append("}\n\n");

        writeValidDataSource(sb, method, params, validParts, validSrc, ind);
        writeInvalidDataSourceOperation(sb, method, params, validParts, invalidSrc, ind);
    }

    // ── Data sources ──────────────────────────────────────────────────────

    private void writeValidDataSource(StringBuilder sb, MethodMeta method,
                                       List<ParameterMeta> params, List<Object> validParts,
                                       String validSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(validSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");
        sb.append(ind).append("        Arguments.of(\"valid ").append(method.name()).append("\",\n");
        for (int i = 0; i < params.size(); i++) {
            sb.append(ind).append("            ")
              .append(generateValidLiteral(params.get(i)));
            sb.append(i < params.size() - 1 ? ",\n" : ")\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    private void writeInvalidDataSource(StringBuilder sb, MethodMeta method,
                                         List<ParameterMeta> params, List<Object> validParts,
                                         String invalidSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");
        // For single param: null + boundary invalids
        ParameterMeta p = params.get(0);
        sb.append(ind).append("        Arguments.of(\"null ").append(p.name()).append("\", ")
          .append(nullCast(p.type())).append(")");
        List<String> extraInvalids = generateInvalidLiterals(p);
        for (String inv : extraInvalids) {
            sb.append(",\n");
            sb.append(ind).append("        Arguments.of(\"invalid ").append(p.name())
              .append("\", ").append(inv).append(")");
        }
        sb.append("\n");
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    private void writeInvalidDataSourceOperation(StringBuilder sb, MethodMeta method,
                                                   List<ParameterMeta> params,
                                                   List<Object> validParts,
                                                   String invalidSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");
        // One row per parameter — null that parameter, valid for rest
        for (int nullIdx = 0; nullIdx < params.size(); nullIdx++) {
            ParameterMeta nullParam = params.get(nullIdx);
            sb.append(ind).append("        Arguments.of(\"null ").append(nullParam.name()).append("\",\n");
            for (int i = 0; i < params.size(); i++) {
                String val = i == nullIdx
                        ? nullCast(params.get(i).type())
                        : generateValidLiteral(params.get(i));
                sb.append(ind).append("            ").append(val);
                sb.append(i < params.size() - 1 ? ",\n" : ")");
            }
            sb.append(nullIdx < params.size() - 1 ? ",\n" : "\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    // ── Value generation ──────────────────────────────────────────────────

    private String generateValidLiteral(ParameterMeta param) {
        Class<?> type = param.type();
        if (type.isPrimitive()) return primitiveDefault(type);
        try {
            long seed = (long) param.name().hashCode();
            Object value = DataFactory.of(type).seed(seed).valid();
            String literal = LiteralConverter.toLiteralOrNull(value);
            if (literal != null) return literal;
        } catch (Exception ignored) {}
        // Fallback for types DataFactory can't generate
        return nullCast(type);
    }

    private List<String> generateInvalidLiterals(ParameterMeta param) {
        Class<?> type = param.type();
        List<String> result = new ArrayList<>();
        // Primitives — add boundary invalids
        if (type == double.class || type == Double.class) {
            result.add("0.0d");
            result.add("-1.0d");
        } else if (type == float.class || type == Float.class) {
            result.add("0.0f");
            result.add("-1.0f");
        } else if (type == int.class || type == Integer.class
                || type == long.class || type == Long.class) {
            result.add("-1");
            result.add("0");
        } else if (type == String.class) {
            result.add("\"\"");
            result.add("\"   \"");
        }
        // Domain objects: try DataFactory invalidList
        if (!type.isPrimitive()) {
            try {
                List<?> invalids = DataFactory.of(type).invalidList();
                for (Object inv : invalids) {
                    String lit = LiteralConverter.toLiteralOrNull(inv);
                    if (lit != null && result.size() < 3) result.add(lit);
                }
            } catch (Exception ignored) {}
        }
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

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
        if (type.isPrimitive()) return primitiveDefault(type);
        return "(" + FieldTestWriter.sourceTypeName(type) + ") null";
    }

    private String primitiveDefault(Class<?> type) {
        return switch (type.getName()) {
            case "boolean" -> "false";
            case "byte"    -> "(byte) 0";
            case "short"   -> "(short) 0";
            case "int"     -> "0";
            case "long"    -> "0L";
            case "float"   -> "0.0f";
            case "double"  -> "0.0d";
            case "char"    -> "'\\0'";
            default        -> "0";
        };
    }
}
