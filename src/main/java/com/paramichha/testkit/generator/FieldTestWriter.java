package com.paramichha.testkit.generator;

import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.FieldMeta;
import com.paramichha.utils.Texts;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Writes one {@code @Nested} class per field.
 * <p>
 * Valid values: always fetched directly from DataFactory.field().validAll()
 * with valid() fallback — never empty, never dependent on validAllPerField
 * having 2+ variants.
 * <p>
 * Naming convention — token = capitalized field name:
 * Class:        {Token}Field
 * Test method:  valid{Token}()  /  invalid{Token}()
 * Data source:  valid{Token}s() /  invalid{Token}s()
 */
final class FieldTestWriter {

    private final ClassMeta meta;
    private final InstantiationWriter instantiation;
    private final ValueExtractor valueExtractor;

    FieldTestWriter(ClassMeta meta, InstantiationWriter instantiation, ValueExtractor valueExtractor) {
        this.meta = meta;
        this.instantiation = instantiation;
        this.valueExtractor = valueExtractor;
    }

    void write(StringBuilder sb, Class<?> type, List<Object> validInstanceParts,
               Map<String, List<?>> validAllPerField) {

        boolean isRecord = meta.constructorStyle() == ClassMeta.ConstructorStyle.RECORD;
        String outerType = meta.simpleName();
        List<FieldMeta> fields = meta.fields();

        for (int i = 0; i < fields.size(); i++) {
            FieldMeta field = fields.get(i);
            boolean hasJakarta = !field.validations().isEmpty();

            // fetch directly — bypasses DefaultDataBuilder's <= 1 skip
            List<?> validValues = instantiation.validValuesFor(type, field).stream()
                    .filter(LiteralConverter::canSerialize).collect(java.util.stream.Collectors.toList());
            List<?> invalidValues = instantiation.invalidValuesFor(type, field).stream()
                    .filter(LiteralConverter::canSerialize).collect(java.util.stream.Collectors.toList());

            // skip field entirely if no serializable data available
            if (validValues.isEmpty() && invalidValues.isEmpty()) continue;

            String token = Texts.capitalize(field.name());
            String className = token + "Field";
            String boxedTypeName = boxed(field.rawType());
            String annotationLabel = hasJakarta
                    ? field.validations().stream()
                    .map(a -> "@" + a.annotationType().getSimpleName())
                    .collect(Collectors.joining(" "))
                    : "no constraints";

            String validDataMethod = "valid" + token + "s";
            String invalidDataMethod = "invalid" + token + "s";

            int fi = i;

            sb.append("    // ");
            sb.append("=".repeat(73)).append("\n");
            sb.append("    // Field: ").append(field.name())
                    .append(" — ").append(annotationLabel).append("\n");
            sb.append("    // ");
            sb.append("=".repeat(73)).append("\n\n");

            sb.append("    @Nested\n");
            sb.append("    @DisplayName(\"Field: ").append(field.name())
                    .append(" — ").append(annotationLabel).append("\")\n");
            sb.append("    class ").append(className).append(" {\n\n");

            // valid{Token}() — always present, guaranteed ≥1 value
            sb.append("        @ParameterizedTest(name = \"[{index}] {0}\")\n");
            sb.append("        @MethodSource(\"").append(validDataMethod).append("\")\n");
            sb.append("        @DisplayName(\"valid ").append(field.name()).append(" passes\")\n");
            sb.append("        void valid").append(token).append("(")
                    .append(boxedTypeName).append(" value) {\n");

            String call = instantiation.constructorCallWithVar(validInstanceParts, fi, "value");
            String accessor = isRecord
                    ? "req." + field.name() + "()"
                    : "req.get" + token + "()";

            sb.append("            ").append(outerType).append(" req = ").append(call).append(";\n");
            sb.append("            assertThat(").append(accessor).append(").isEqualTo(value);\n");
            if (hasJakarta) {
                sb.append("            assertThat(validator.validate(req)).isEmpty();\n");
            }
            sb.append("        }\n\n");

            // valid{Token}s() — use Arrays.asList in case value is null (@Null constraint)
            boolean validHasNull = validValues.stream().anyMatch(Objects::isNull);
            sb.append("        static List<").append(boxedTypeName).append("> ")
                    .append(validDataMethod).append("() {\n");
            if (validHasNull) {
                sb.append("            return java.util.Arrays.asList(")
                        .append(castNullsInList(validValues, boxedTypeName)).append(");\n");
            } else {
                sb.append("            return List.of(")
                        .append(literalList(validValues)).append(");\n");
            }
            sb.append("        }\n\n");

            // invalid{Token}() — only emit when there are actual invalid values to test
            if (!invalidValues.isEmpty()) {
                sb.append("        @ParameterizedTest(name = \"[{index}] {0}\")\n");
                sb.append("        @MethodSource(\"").append(invalidDataMethod).append("\")\n");
                sb.append("        @DisplayName(\"invalid ").append(field.name()).append(" fails\")\n");
                sb.append("        void invalid").append(token).append("(")
                        .append(boxedTypeName).append(" value) {\n");

                if (hasJakarta) {
                    sb.append("            assertThat(validator.validate(\n");
                    sb.append("                ").append(instantiation.constructorCallWithVar(
                            validInstanceParts, fi, "value")).append("\n");
                    sb.append("            )).isNotEmpty();\n");
                } else {
                    sb.append("            // no constraints — value accepted as-is\n");
                }
                sb.append("        }\n\n");

                // invalid{Token}s() — may contain nulls, use Arrays.asList
                sb.append("        static List<").append(boxedTypeName).append("> ")
                        .append(invalidDataMethod).append("() {\n");
                sb.append("            return java.util.Arrays.asList(")
                        .append(castNullsInList(invalidValues, boxedTypeName)).append(");\n");
                sb.append("        }\n");
            }

            sb.append("    }\n\n");
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static String boxed(Class<?> type) {
        if (!type.isPrimitive()) return sourceTypeName(type);
        return switch (type.getName()) {
            case "boolean" -> "Boolean";
            case "byte" -> "Byte";
            case "short" -> "Short";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            case "char" -> "Character";
            default -> sourceTypeName(type);
        };
    }

    static String sourceTypeName(Class<?> type) {
        if (type.getEnclosingClass() != null) {
            return type.getEnclosingClass().getSimpleName() + "." + type.getSimpleName();
        }
        return type.getSimpleName();
    }

    private String literalList(List<?> values) {
        return values.stream().map(LiteralConverter::toLiteral).collect(Collectors.joining(", "));
    }

    /**
     * Like literalList but wraps bare null entries with an explicit cast so
     * Arrays.asList(null) is not ambiguous (it would be resolved as Object[] null → NPE).
     * Example: castNullsInList([null], "UUID") → "(UUID) null"
     */
    private String castNullsInList(List<?> values, String boxedType) {
        return values.stream()
                .map(v -> v == null ? "(" + boxedType + ") null" : LiteralConverter.toLiteral(v))
                .collect(Collectors.joining(", "));
    }

}
