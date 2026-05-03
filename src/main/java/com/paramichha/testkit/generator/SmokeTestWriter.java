package com.paramichha.testkit.generator;

import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.FieldMeta;
import com.paramichha.testkit.model.TestData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes the {@code SmokeTest} nested class.
 *
 * Always emits five methods — assertion body adapts based on Jakarta presence,
 * method names never change. Objects that cannot be expressed as source literals
 * (nested domain objects, etc.) are filtered out of the data source lists —
 * JUnit auto-skips a @ParameterizedTest whose source returns empty.
 */
final class SmokeTestWriter {

    private final ClassMeta meta;
    private final InstantiationWriter instantiation;

    SmokeTestWriter(ClassMeta meta, InstantiationWriter instantiation) {
        this.meta = meta;
        this.instantiation = instantiation;
    }

    void write(StringBuilder sb, TestData data, List<Object> validInstanceParts,
               ValueExtractor valueExtractor) {

        boolean hasJakarta    = data.hasJakarta(meta);
        boolean isRecord      = meta.constructorStyle() == ClassMeta.ConstructorStyle.RECORD;
        String  typeName      = meta.simpleName();
        String  validData     = "valid" + typeName + "s";
        String  invalidData   = "invalid" + typeName + "s";

        // pre-filter: only keep objects whose all field values can be serialized
        List<?> serializableValid = data.validObjects().stream()
                .filter(v -> canSerializeAll(v, data, valueExtractor))
                .collect(Collectors.toList());

        List<?> serializableInvalid = data.violations().stream()
                .filter(v -> canSerializeAll(v, data, valueExtractor))
                .collect(Collectors.toList());

        sb.append("    @Nested\n");
        sb.append("    @DisplayName(\"Smoke\")\n");
        sb.append("    class SmokeTest {\n\n");

        // 1. validInstanceIsCreated — always
        sb.append("        @Test\n");
        sb.append("        @DisplayName(\"validInstance is created\")\n");
        sb.append("        void validInstanceIsCreated() {\n");
        sb.append("            assertThat(validInstance).isNotNull();\n");
        sb.append("        }\n\n");

        // 2. validInstanceFieldsAreCorrect — always, one assertion per serializable field
        sb.append("        @Test\n");
        sb.append("        @DisplayName(\"validInstance fields hold expected values\")\n");
        sb.append("        void validInstanceFieldsAreCorrect() {\n");
        for (int i = 0; i < meta.fields().size(); i++) {
            FieldMeta field = meta.fields().get(i);
            Object value = i < validInstanceParts.size() ? validInstanceParts.get(i) : null;
            if (!LiteralConverter.canSerialize(value)) continue; // skip complex fields
            String accessor = isRecord
                    ? "validInstance." + field.name() + "()"
                    : "validInstance.get" + capitalize(field.name()) + "()";
            String expected = LiteralConverter.toLiteral(value);
            sb.append("            assertThat(").append(accessor)
              .append(").isEqualTo(").append(expected).append(");\n");
        }
        sb.append("        }\n\n");

        // 3. validInstancePassesValidation — always, assertion differs
        sb.append("        @Test\n");
        sb.append("        @DisplayName(\"validInstance passes validation\")\n");
        sb.append("        void validInstancePassesValidation() {\n");
        if (hasJakarta) {
            sb.append("            assertThat(validator.validate(validInstance)).isEmpty();\n");
        } else {
            sb.append("            assertThat(validInstance).isNotNull();\n");
        }
        sb.append("        }\n\n");

        // 4. valid{ClassName}() — always, JUnit skips if list empty
        sb.append("        @ParameterizedTest(name = \"[{index}] {0}\")\n");
        sb.append("        @MethodSource(\"").append(validData).append("\")\n");
        sb.append("        @DisplayName(\"valid ").append(typeName).append(" passes validation\")\n");
        sb.append("        void valid").append(typeName).append("(").append(typeName).append(" req) {\n");
        sb.append("            assertThat(req).isNotNull();\n");
        if (hasJakarta) {
            sb.append("            assertThat(validator.validate(req)).isEmpty();\n");
        }
        sb.append("        }\n\n");

        sb.append("        static List<").append(typeName).append("> ").append(validData).append("() {\n");
        sb.append("            return List.of(\n");
        String validLines = serializableValid.stream()
                .map(v -> "                " + instantiation.constructorCall(
                        valueExtractor.extract(v, data.type(), meta.fields())))
                .collect(Collectors.joining(",\n"));
        sb.append(validLines).append(validLines.isEmpty() ? "" : "\n");
        sb.append("            );\n");
        sb.append("        }\n\n");

        // 5. invalid{ClassName}() — only emit when violations exist
        // A @ParameterizedTest with zero arguments throws PreconditionViolation
        if (!serializableInvalid.isEmpty()) {
            sb.append("        @ParameterizedTest(name = \"[{index}] {0}\")\n");
            sb.append("        @MethodSource(\"").append(invalidData).append("\")\n");
            sb.append("        @DisplayName(\"invalid ").append(typeName).append(" fails validation\")\n");
            sb.append("        void invalid").append(typeName).append("(").append(typeName).append(" req) {\n");
            if (hasJakarta) {
                sb.append("            assertThat(validator.validate(req)).isNotEmpty();\n");
            } else {
                sb.append("            assertThat(req).isNotNull();\n");
            }
            sb.append("        }\n\n");

            sb.append("        static List<").append(typeName).append("> ").append(invalidData).append("() {\n");
            sb.append("            return java.util.Arrays.asList(\n");
            String invalidLines = serializableInvalid.stream()
                    .map(v -> "                " + instantiation.constructorCall(
                            valueExtractor.extract(v, data.type(), meta.fields())))
                    .collect(Collectors.joining(",\n"));
            sb.append(invalidLines).append("\n");
            sb.append("            );\n");
            sb.append("        }\n");
        }

        sb.append("    }\n\n");
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private boolean canSerializeAll(Object obj, TestData data, ValueExtractor ve) {
        List<Object> parts = ve.extract(obj, data.type(), meta.fields());
        return parts.stream().allMatch(LiteralConverter::canSerialize);
    }

    private static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
