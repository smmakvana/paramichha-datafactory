package com.paramichha.testkit.generator;

import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.FieldMeta;
import com.paramichha.testkit.model.TestData;
import com.paramichha.testkit.template.TemplateResolver;

import java.util.List;

/**
 * Writes the {ClassName}CreationTest nested class.
 *
 * <p>Always generated. Contains:
 * <ul>
 *   <li>{@code canBeCreated()} — proves the object can be instantiated
 *   <li>{@code holdsExpectedValues()} — one assert per serializable primitive field
 *   <li>{@code isValid()} — only if class has Jakarta annotations
 * </ul>
 */
final class CreationTestWriter {

    private final ClassMeta meta;
    private final InstantiationWriter instantiation;
    private final TestKitTemplate template;

    CreationTestWriter(ClassMeta meta, InstantiationWriter instantiation,
                       TestKitTemplate template) {
        this.meta          = meta;
        this.instantiation = instantiation;
        this.template      = template;
    }

    void write(StringBuilder sb, TestData data, List<Object> validInstanceParts,
               ValueExtractor valueExtractor) {

        boolean hasJakarta  = data.hasJakarta(meta);
        boolean isRecord    = meta.constructorStyle() == ClassMeta.ConstructorStyle.RECORD;
        String  className   = meta.simpleName();
        String  fieldName   = TemplateResolver.instanceFieldName(className);

        String nestedClass  = template.resolve(template.creationClassName, className, "");
        String nestedDisplay = template.resolve(template.creationDisplay, className, "");

        sb.append("    // ").append("=".repeat(73)).append("\n");
        sb.append("    // ").append(nestedDisplay).append("\n");
        sb.append("    // ").append("=".repeat(73)).append("\n\n");

        sb.append("    @Nested\n");
        sb.append("    @DisplayName(\"").append(nestedDisplay).append("\")\n");
        sb.append("    class ").append(nestedClass).append(" {\n\n");

        // canBeCreated — always
        String canBeCreatedMethod  = template.resolve(template.canBeCreated, className, "");
        String canBeCreatedDisplay = template.resolve(template.canBeCreatedDisplay, className, "");
        sb.append("        @Test\n");
        sb.append("        @DisplayName(\"").append(canBeCreatedDisplay).append("\")\n");
        sb.append("        void ").append(canBeCreatedMethod).append("() {\n");
        sb.append("            assertThat(").append(fieldName).append(").isNotNull();\n");
        sb.append("        }\n\n");

        // holdsExpectedValues — always, one assert per serializable primitive field
        String holdsMethod  = template.resolve(template.holdsExpectedValues, className, "");
        String holdsDisplay = template.resolve(template.holdsExpectedDisplay, className, "");
        sb.append("        @Test\n");
        sb.append("        @DisplayName(\"").append(holdsDisplay).append("\")\n");
        sb.append("        void ").append(holdsMethod).append("() {\n");
        for (int i = 0; i < meta.fields().size(); i++) {
            FieldMeta field = meta.fields().get(i);
            Object value = i < validInstanceParts.size() ? validInstanceParts.get(i) : null;
            if (!LiteralConverter.canSerialize(value)) continue;
            if (isComplexType(field.rawType())) continue;
            String accessor = isRecord
                    ? fieldName + "." + field.name() + "()"
                    : fieldName + ".get" + TemplateResolver.capitalize(field.name()) + "()";
            String expected = LiteralConverter.toLiteral(value);
            sb.append("            assertThat(").append(accessor)
              .append(").isEqualTo(").append(expected).append(");\n");
        }
        sb.append("        }\n");

        // isValid — only if has Jakarta annotations
        if (hasJakarta) {
            String isValidMethod  = template.resolve(template.isValid, className, "");
            String isValidDisplay = template.resolve(template.isValidDisplay, className, "");
            sb.append("\n        @Test\n");
            sb.append("        @DisplayName(\"").append(isValidDisplay).append("\")\n");
            sb.append("        void ").append(isValidMethod).append("() {\n");
            sb.append("            assertThat(").append(template.validatorField)
              .append(".validate(").append(fieldName).append(")).isEmpty();\n");
            sb.append("        }\n");
        }

        sb.append("    }\n\n");
    }

    private static boolean isComplexType(Class<?> type) {
        if (type.isPrimitive()) return false;
        String pkg = type.getPackageName();
        return !pkg.startsWith("java.") && !pkg.startsWith("jakarta.")
               && !type.isEnum() && type != String.class;
    }
}
