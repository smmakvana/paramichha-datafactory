package com.paramichha.testkit.generator;

import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.FieldMeta;
import com.paramichha.testkit.model.TestData;
import com.paramichha.testkit.utils.TestNames;
import com.paramichha.testkit.utils.JavaNames;

import java.util.List;

/**
 * Writes the {ClassName}CreationTest nested class.
 * In split mode ({@link #writeFlat}), writes content without the @Nested wrapper.
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

    /** Standard mode — writes @Nested block inside the outer test class. */
    void write(StringBuilder sb, TestData data, List<Object> validInstanceParts,
               ValueExtractor valueExtractor) {
        writeContent(sb, data, validInstanceParts, true);
    }

    /** Split mode — writes content directly without the @Nested wrapper. */
    void writeFlat(StringBuilder sb, TestData data, List<Object> validInstanceParts,
                   ValueExtractor valueExtractor) {
        writeContent(sb, data, validInstanceParts, false);
    }

    private void writeContent(StringBuilder sb, TestData data,
                              List<Object> validInstanceParts, boolean nested) {

        boolean hasJakarta   = data.hasJakarta(meta);
        boolean isRecord     = meta.constructorStyle() == ClassMeta.ConstructorStyle.RECORD;
        String  className    = meta.simpleName();
        String  fieldName    = TestNames.toVariable(className);
        String  ind          = nested ? "        " : "    ";
        String  indBody      = nested ? "            " : "        ";
        String  nestedClass  = template.resolve(template.creationClassName, className, "");
        String  nestedDisplay = template.resolve(template.creationDisplay, className, "");

        if (nested) {
            sb.append("    // ").append("=".repeat(73)).append("\n");
            sb.append("    // ").append(nestedDisplay).append("\n");
            sb.append("    // ").append("=".repeat(73)).append("\n\n");
            sb.append("    @Nested\n");
            sb.append("    @DisplayName(\"").append(nestedDisplay).append("\"\n");
            sb.append("    class ").append(nestedClass).append(" {\n\n");
        }

        // canBeCreated
        String canBeCreatedMethod  = template.resolve(template.canBeCreated, className, "");
        String canBeCreatedDisplay = template.resolve(template.canBeCreatedDisplay, className, "");
        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"").append(canBeCreatedDisplay).append("\"\n");
        sb.append(ind).append("void ").append(canBeCreatedMethod).append("() {\n");
        sb.append(indBody).append("assertThat(").append(fieldName).append(").isNotNull();\n");
        sb.append(ind).append("}\n\n");

        // holdsExpectedValues
        String holdsMethod  = template.resolve(template.holdsExpectedValues, className, "");
        String holdsDisplay = template.resolve(template.holdsExpectedDisplay, className, "");
        sb.append(ind).append("@Test\n");
        sb.append(ind).append("@DisplayName(\"").append(holdsDisplay).append("\"\n");
        sb.append(ind).append("void ").append(holdsMethod).append("() {\n");
        for (int i = 0; i < meta.fields().size(); i++) {
            FieldMeta field = meta.fields().get(i);
            Object value = i < validInstanceParts.size() ? validInstanceParts.get(i) : null;
            if (!LiteralConverter.canSerialize(value)) continue;
            if (isComplexType(field.rawType())) continue;
            String accessor = isRecord
                    ? fieldName + "." + field.name() + "()"
                    : fieldName + "." + JavaNames.getterName(field.name(), field.rawType()) + "()";
            String expected = LiteralConverter.toLiteral(value);
            sb.append(indBody).append("assertThat(").append(accessor)
                    .append(").isEqualTo(").append(expected).append(");\n");
        }
        sb.append(ind).append("}\n");

        // isValid — only if class has Jakarta annotations
        if (hasJakarta) {
            String isValidMethod  = template.resolve(template.isValid, className, "");
            String isValidDisplay = template.resolve(template.isValidDisplay, className, "");
            sb.append("\n").append(ind).append("@Test\n");
            sb.append(ind).append("@DisplayName(\"").append(isValidDisplay).append("\"\n");
            sb.append(ind).append("void ").append(isValidMethod).append("() {\n");
            sb.append(indBody).append("assertThat(").append(template.validatorField)
                    .append(".validate(").append(fieldName).append(")).isEmpty();\n");
            sb.append(ind).append("}\n");
        }

        if (nested) sb.append("    }\n\n");
    }

    private static boolean isComplexType(Class<?> type) {
        if (type.isPrimitive()) return false;
        String pkg = type.getPackageName();
        return !pkg.startsWith("java.") && !pkg.startsWith("jakarta.")
                && !type.isEnum() && type != String.class;
    }
}