package com.paramichha.testkit.generator;

import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.FieldMeta;
import com.paramichha.testkit.template.TemplateResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes one {MemberName}Test nested class per field.
 *
 * <p>Rules:
 * <ul>
 *   <li>Always generated for every field — including unannotated fields
 *   <li>Has annotations: accepts + rejects parameterized tests (both live)
 *   <li>No annotations: accepts test only; rejects test generated as commented-out template
 * </ul>
 *
 * <p>Body pattern: rebuild root valid object substituting only the tested field.
 * <pre>
 *   Circle c = new Circle(name, circle.center(), circle.radius());
 * </pre>
 */
final class MemberTestWriter {

    private final ClassMeta meta;
    private final InstantiationWriter instantiation;
    private final ValueExtractor valueExtractor;
    private final TestKitTemplate template;

    MemberTestWriter(ClassMeta meta, InstantiationWriter instantiation,
                     ValueExtractor valueExtractor, TestKitTemplate template) {
        this.meta          = meta;
        this.instantiation = instantiation;
        this.valueExtractor = valueExtractor;
        this.template      = template;
    }

    void write(StringBuilder sb, Class<?> type, List<Object> validInstanceParts,
               Map<String, List<?>> validAllPerField) {

        boolean isRecord  = meta.constructorStyle() == ClassMeta.ConstructorStyle.RECORD;
        String  className = meta.simpleName();
        String  fieldName = TemplateResolver.instanceFieldName(className);
        List<FieldMeta> fields = meta.fields();

        for (int i = 0; i < fields.size(); i++) {
            FieldMeta field    = fields.get(i);
            boolean hasJakarta = !field.validations().isEmpty();
            String  memberName = field.name();
            String  boxedType  = boxed(field.rawType());

            List<?> validValues = instantiation.validValuesFor(type, field).stream()
                    .filter(LiteralConverter::canSerialize)
                    .collect(Collectors.toList());
            List<?> invalidValues = instantiation.invalidValuesFor(type, field).stream()
                    .filter(LiteralConverter::canSerialize)
                    .collect(Collectors.toList());

            // Skip if nothing to test at all
            if (validValues.isEmpty() && invalidValues.isEmpty()) continue;

            String nestedClass   = template.resolve(template.memberClassName, className, memberName);
            String nestedDisplay = template.resolve(template.memberDisplay,   className, memberName);
            String acceptsMethod = template.resolve(template.acceptsMethod,   className, memberName);
            String acceptsDisplay = template.resolve(template.acceptsDisplay, className, memberName);
            String rejectsMethod = template.resolve(template.rejectsMethod,   className, memberName);
            String rejectsDisplay = template.resolve(template.rejectsDisplay, className, memberName);
            String validSrc      = template.resolve(template.validDataSource,   className, memberName);
            String invalidSrc    = template.resolve(template.invalidDataSource, className, memberName);

            int fi = i;

            sb.append("    // ").append("=".repeat(73)).append("\n");
            sb.append("    // ").append(nestedDisplay).append("\n");
            sb.append("    // ").append("=".repeat(73)).append("\n\n");

            sb.append("    @Nested\n");
            sb.append("    @DisplayName(\"").append(nestedDisplay).append("\")\n");
            sb.append("    class ").append(nestedClass).append(" {\n\n");

            // ── accepts — always live ─────────────────────────────────────
            if (!validValues.isEmpty()) {
                sb.append("        @ParameterizedTest(name = \"{0}\")\n");
                sb.append("        @MethodSource(\"").append(validSrc).append("\")\n");
                sb.append("        @DisplayName(\"").append(acceptsDisplay).append("\")\n");
                sb.append("        void ").append(acceptsMethod)
                  .append("(String description, ").append(boxedType).append(" ").append(memberName).append(") {\n");
                sb.append("            ").append(className).append(" obj = ")
                  .append(instantiation.constructorCallWithVar(validInstanceParts, fi, memberName)).append(";\n");
                if (hasJakarta) {
                    sb.append("            assertThat(").append(template.validatorField)
                      .append(".validate(obj)).isEmpty();\n");
                } else {
                    sb.append("            assertThat(obj.").append(accessorFor(field, isRecord))
                      .append(").isEqualTo(").append(memberName).append(");\n");
                }
                sb.append("        }\n\n");

                // valid data source
                boolean hasNull = validValues.stream().anyMatch(v -> v == null);
                sb.append("        static Stream<Arguments> ").append(validSrc).append("() {\n");
                sb.append("            return Stream.of(\n");
                for (int j = 0; j < validValues.size(); j++) {
                    Object v = validValues.get(j);
                    String label = labelFor(v, memberName, "valid", j);
                    String literal = v == null ? "(" + boxedType + ") null" : LiteralConverter.toLiteral(v);
                    sb.append("                Arguments.of(\"").append(label).append("\", ").append(literal).append(")");
                    sb.append(j < validValues.size() - 1 ? ",\n" : "\n");
                }
                sb.append("            );\n");
                sb.append("        }\n\n");
            }

            // ── rejects — live if annotations, commented if none ──────────
            if (hasJakarta && !invalidValues.isEmpty()) {
                sb.append("        @ParameterizedTest(name = \"{0}\")\n");
                sb.append("        @MethodSource(\"").append(invalidSrc).append("\")\n");
                sb.append("        @DisplayName(\"").append(rejectsDisplay).append("\")\n");
                sb.append("        void ").append(rejectsMethod)
                  .append("(String description, ").append(boxedType).append(" ").append(memberName).append(") {\n");
                sb.append("            ").append(className).append(" obj = ")
                  .append(instantiation.constructorCallWithVar(validInstanceParts, fi, memberName)).append(";\n");
                sb.append("            assertThat(").append(template.validatorField)
                  .append(".validate(obj)).isNotEmpty();\n");
                sb.append("        }\n\n");

                sb.append("        static Stream<Arguments> ").append(invalidSrc).append("() {\n");
                sb.append("            return Stream.of(\n");
                for (int j = 0; j < invalidValues.size(); j++) {
                    Object v = invalidValues.get(j);
                    String label = labelFor(v, memberName, "invalid", j);
                    String literal = v == null ? "(" + boxedType + ") null" : LiteralConverter.toLiteral(v);
                    sb.append("                Arguments.of(\"").append(label).append("\", ").append(literal).append(")");
                    sb.append(j < invalidValues.size() - 1 ? ",\n" : "\n");
                }
                sb.append("            );\n");
                sb.append("        }\n\n");

            } else if (!hasJakarta) {
                // Commented out template — ready to fill when constraints are added
                sb.append("        // TODO: add constraints to ").append(memberName)
                  .append(" then uncomment\n");
                sb.append("        // @ParameterizedTest(name = \"{0}\")\n");
                sb.append("        // @MethodSource(\"").append(invalidSrc).append("\")\n");
                sb.append("        // @DisplayName(\"").append(rejectsDisplay).append("\")\n");
                sb.append("        // void ").append(rejectsMethod)
                  .append("(String description, ").append(boxedType).append(" ").append(memberName).append(") {\n");
                sb.append("        //     ").append(className).append(" obj = ")
                  .append(instantiation.constructorCallWithVar(validInstanceParts, fi, memberName)).append(";\n");
                sb.append("        //     assertThat(validator.validate(obj)).isNotEmpty();\n");
                sb.append("        // }\n\n");

                sb.append("        // static Stream<Arguments> ").append(invalidSrc).append("() {\n");
                sb.append("        //     return Stream.of(\n");
                sb.append("        //         // Arguments.of(\"reason\", value)\n");
                sb.append("        //     );\n");
                sb.append("        // }\n\n");
            }

            sb.append("    }\n\n");
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static String accessorFor(FieldMeta field, boolean isRecord) {
        return isRecord
            ? field.name() + "()"
            : "get" + TemplateResolver.capitalize(field.name()) + "()";
    }

    private static String labelFor(Object value, String memberName, String kind, int index) {
        if (value == null) return "null " + memberName;
        if (value instanceof String s) return s.isEmpty() ? "blank " + memberName : memberName + " " + index;
        return kind + " " + memberName + " " + (index + 1);
    }

    private static String boxed(Class<?> type) {
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
            default        -> FieldTestWriter.sourceTypeName(type);
        };
    }
}
