package com.paramichha.testkit.generator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Produces source code for a complete Java class — top-level or nested.
 * Recurses for nested classes. Handles {@link ClassCodingInfo#rawBody()} escape hatch.
 */
public final class ClassCoder implements Coder<ClassCodingInfo> {

    private static final int TAB_SIZE = 4;
    private static final String INDENT      = " ".repeat(TAB_SIZE);
    private static final FieldCoder  FIELD_CODER  = new FieldCoder();
    private static final MethodCoder METHOD_CODER = new MethodCoder();

    @Override
    public String code(ClassCodingInfo spec) {
        StringBuilder sb = new StringBuilder();

        // Top-level only: header + package + imports
        if (!spec.isNested()) {
            if (spec.header() != null && !spec.header().isBlank())
                sb.append(spec.header());
            if (spec.packageName() != null && !spec.packageName().isBlank())
                sb.append("package ").append(spec.packageName()).append(";\n\n");
            List<String> imports = spec.imports();
            if (!imports.isEmpty()) {
                imports.forEach(i -> sb.append("import ").append(i).append(";\n"));
                sb.append("\n");
            }
        }

        // Annotations
        String prefix = spec.isNested() ? INDENT : "";
        for (String ann : spec.annotations())
            sb.append(prefix).append(ann).append("\n");

        // Class declaration
        sb.append(prefix);
        for (String mod : spec.modifiers())
            sb.append(mod).append(" ");
        sb.append(spec.className());
        if (spec.extendsClass() != null && !spec.extendsClass().isBlank())
            sb.append(" extends ").append(spec.extendsClass());
        sb.append(" {\n\n");

        // Body — rawBody takes priority over structured fields/methods/nestedClasses
        if (spec.rawBody() != null) {
            sb.append(indent(spec.rawBody(), spec.isNested()));
        } else {
            // Fields
            for (FieldCodingInfo field : spec.fields())
                sb.append(indent(FIELD_CODER.code(field), spec.isNested()));

            if (!spec.fields().isEmpty() && (!spec.methods().isEmpty() || !spec.nestedClasses().isEmpty()))
                sb.append("\n");

            // Methods
            for (MethodCodingInfo method : spec.methods())
                sb.append(indent(METHOD_CODER.code(method), spec.isNested()));

            // Nested classes — recursive
            for (ClassCodingInfo nested : spec.nestedClasses())
                sb.append(indent(code(nested), spec.isNested()));
        }

        sb.append(prefix).append("}\n");
        if (spec.isNested()) sb.append("\n");

        return sb.toString();
    }

    private static String indent(String src, boolean nested) {
        if (!nested || src == null || src.isBlank()) return src == null ? "" : src;
        return src.lines()
                .map(line -> line.isBlank() ? line : INDENT + line)
                .collect(Collectors.joining("\n", "", "\n"));
    }
}
