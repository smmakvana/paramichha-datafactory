package com.paramichha.testkit.generator;

/**
 * Produces source code for a single field declaration.
 *
 * <pre>
 * // "    private static Validator validator;\n"
 * // "    ObjectMixedService objectMixedService =\n            new ObjectMixedService(...);\n"
 * </pre>
 */
public final class FieldCoder implements Coder<FieldCodingInfo> {

    private static final String INDENT = "    ";

    @Override
    public String code(FieldCodingInfo spec) {
        StringBuilder sb = new StringBuilder();

        // Annotations — one per line
        for (String ann : spec.annotations())
            sb.append(INDENT).append(ann).append("\n");

        // Modifiers + type + name
        sb.append(INDENT);
        for (String mod : spec.modifiers())
            sb.append(mod).append(" ");
        sb.append(spec.typeName()).append(" ").append(spec.name());

        // Initializer
        if (spec.initializer() != null && !spec.initializer().isBlank()) {
            sb.append(" =\n").append(INDENT).append("        ").append(spec.initializer());
        }

        sb.append(";\n");
        return sb.toString();
    }
}
