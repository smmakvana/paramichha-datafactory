package com.paramichha.testkit.generator;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.MethodKind;
import com.paramichha.testkit.model.MethodMeta;
import com.paramichha.testkit.model.ParameterMeta;
import com.paramichha.testkit.utils.TestNames;

import java.util.List;
import java.util.Map;

/**
 * Writes one {ClassName}Method{MethodName}Test nested class per public method.
 *
 * <p>Generated tests are self-contained — no DataFactory dependency in output.
 * DataFactory is called here at generation time to produce hardcoded literals.
 * Uses the same field-level API as MemberTestWriter for consistent value generation.
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

    void write(StringBuilder sb, List<Object> validInstanceParts, boolean hasJakarta) {
        for (MethodMeta method : meta.methods()) {
            writeMethodClass(sb, method, validInstanceParts, hasJakarta, true);
        }
    }

    void writeFlat(StringBuilder sb, MethodMeta method,
                   List<Object> validInstanceParts, boolean hasJakarta) {
        writeMethodClass(sb, method, validInstanceParts, hasJakarta, false);
    }

    // ── Core ──────────────────────────────────────────────────────────────

    private void writeMethodClass(StringBuilder sb, MethodMeta method,
                                  List<Object> validInstanceParts,
                                  boolean hasJakarta, boolean nested) {

        String className   = meta.simpleName();
        String fieldName   = meta.utilityClass() ? className : TestNames.toVariable(className);
        String methodName  = method.name();
        String ind         = nested ? "        " : "    ";
        String indBody     = nested ? "            " : "        ";

        String nestedClass   = template.resolveMethod(template.methodClassName,   className, methodName);
        String nestedDisplay = template.resolveMethod(template.methodDisplay,      className, methodName);
        String validSrc      = template.resolveMethod(template.validInputsSource,  className, methodName);
        String invalidSrc    = template.resolveMethod(template.invalidInputsSource, className, methodName);

        sb.append("    // ").append("=".repeat(73)).append("\n");
        sb.append("    // ").append(nestedDisplay).append("\n");
        sb.append("    // ").append("=".repeat(73)).append("\n\n");

        if (nested) {
            sb.append("    @Nested\n");
            sb.append("    @DisplayName(\"").append(nestedDisplay).append("\")\n");
            sb.append("    class ").append(nestedClass).append(" {\n\n");
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

    private void writeCommand(StringBuilder sb, MethodMeta method, String fieldName,
                              boolean hasJakarta, String ind, String indBody) {
        String methodName = method.name();

        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"executes without exception\")\n");
        sb.append(ind).append("void ").append(template.testExecutesWithoutException).append("() {\n");
        sb.append(indBody).append("assertThatCode(() -> ")
                .append(fieldName).append(".").append(methodName).append("()).doesNotThrow();\n");
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
        String call = fieldName + "." + methodName + "()";

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
        String methodName = method.name();
        List<ParameterMeta> params = method.parameters();
        String paramSignature = buildParamSignature(params);
        String paramCall      = buildParamCall(params);

        // testAcceptsValidInput — always
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

        // testRejectsInvalidInput — only if any parameter is constrained
        boolean anyConstrained = params.stream().anyMatch(ParameterMeta::isConstrained);
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
        if (anyConstrained) {
            writeInvalidDataSource(sb, params, invalidSrc, ind);
        }
    }

    // ── OPERATION ─────────────────────────────────────────────────────────

    private void writeOperation(StringBuilder sb, MethodMeta method,
                                String fieldName,
                                String validSrc, String invalidSrc,
                                String ind, String indBody) {
        String methodName = method.name();
        List<ParameterMeta> params = method.parameters();
        String paramSignature = buildParamSignature(params);
        String paramCall      = buildParamCall(params);

        // testAcceptsValidInput — always
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

        // testRejectsInvalidInput — only if any parameter is constrained
        boolean anyConstrained = params.stream().anyMatch(ParameterMeta::isConstrained);
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
        if (anyConstrained) {
            writeInvalidDataSourceOperation(sb, params, invalidSrc, ind);
        }
    }

    // ── Data sources — DataFactory field-level API ────────────────────────

    private void writeValidDataSource(StringBuilder sb, MethodMeta method,
                                      List<ParameterMeta> params,
                                      String validSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(validSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");
        sb.append(ind).append("        Arguments.of(\"valid ").append(method.name()).append("\",\n");
        for (int i = 0; i < params.size(); i++) {
            sb.append(ind).append("            ")
                    .append(validLiteralFor(params.get(i)));
            sb.append(i < params.size() - 1 ? ",\n" : ")\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    /**
     * Single-parameter invalid source.
     * Uses DataFactory.of(type).invalidList() — same as MemberTestWriter.
     */
    private void writeInvalidDataSource(StringBuilder sb,
                                        List<ParameterMeta> params,
                                        String invalidSrc, String ind) {
        ParameterMeta p = params.get(0);
        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");

        List<String> invalids = invalidLiteralsFor(p);
        for (int i = 0; i < invalids.size(); i++) {
            sb.append(ind).append("        Arguments.of(\"invalid ").append(p.name())
                    .append(" ").append(i + 1).append("\", ").append(invalids.get(i)).append(")");
            sb.append(i < invalids.size() - 1 ? ",\n" : "\n");
        }
        sb.append(ind).append("    );\n");
        sb.append(ind).append("}\n\n");
    }

    /**
     * Multi-parameter invalid source — one row per parameter, null that one, valid for rest.
     * Only rows where the parameter isConstrained() are included.
     */
    private void writeInvalidDataSourceOperation(StringBuilder sb,
                                                 List<ParameterMeta> params,
                                                 String invalidSrc, String ind) {
        sb.append(ind).append("static Stream<Arguments> ").append(invalidSrc).append("() {\n");
        sb.append(ind).append("    return Stream.of(\n");

        List<String> rows = new java.util.ArrayList<>();
        for (int nullIdx = 0; nullIdx < params.size(); nullIdx++) {
            ParameterMeta nullParam = params.get(nullIdx);
            if (!nullParam.isConstrained()) continue;

            StringBuilder row = new StringBuilder();
            row.append(ind).append("        Arguments.of(\"null ").append(nullParam.name()).append("\",\n");
            for (int i = 0; i < params.size(); i++) {
                String val = i == nullIdx
                        ? nullCast(params.get(i).type())
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

    // ── Value generation — DataFactory field-level API ────────────────────

    /**
     * Valid literal for a parameter.
     * Uses DataFactory.of(type).valid() — same approach as MemberTestWriter valid values.
     */
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

    /**
     * Invalid literals for a parameter.
     * For constrained object types: uses DataFactory.of(type).invalidList()
     * — same as MemberTestWriter invalid values.
     * For directly annotated params: uses validPerField() on the type.
     * For primitives/String: boundary values.
     */
    private List<String> invalidLiteralsFor(ParameterMeta param) {
        Class<?> type = param.type();
        List<String> result = new java.util.ArrayList<>();

        // Direct annotations on param — treat as if it were a field
        if (!param.validations().isEmpty()) {
            if (type == String.class) {
                result.add("(String) null");
                result.add("\"\"");
                result.add("\"   \"");
                return result;
            }
        }

        // Indirect — object type has constrained fields — use DataFactory.invalidList()
        if (param.hasIndirectConstraints() && !type.isPrimitive()) {
            try {
                List<?> invalids = DataFactory.of(type).invalidList();
                for (Object inv : invalids) {
                    String lit = LiteralConverter.toLiteralOrNull(inv);
                    if (lit != null) result.add(lit);
                    if (result.size() >= 3) break;
                }
            } catch (Exception ignored) {}
        }

        // Fallback — null is always invalid for object types
        if (result.isEmpty() && !type.isPrimitive()) {
            result.add(nullCast(type));
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