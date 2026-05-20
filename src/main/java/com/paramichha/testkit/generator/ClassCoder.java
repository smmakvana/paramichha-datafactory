package com.paramichha.testkit.generator;

import java.util.List;

/**
 * Produces source code for a complete Java class — top-level or nested.
 *
 * <p>Checks {@link ClassCodingInfo#isNested()} to decide whether to emit
 * header, package, and imports. Delegates fields, methods, and nested classes
 * to their respective coders. Recurses for nested classes.
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
        for (String ann : spec.annotations())
            sb.append(spec.isNested() ? INDENT : "").append(ann).append("\n");

        // Class declaration
        String prefix = spec.isNested() ? INDENT : "";
        sb.append(prefix);
        for (String mod : spec.modifiers())
            sb.append(mod).append(" ");
        sb.append(spec.className());
        if (spec.extendsClass() != null && !spec.extendsClass().isBlank())
            sb.append(" extends ").append(spec.extendsClass());
        sb.append(" {\n\n");

        // Fields
        for (FieldCodingInfo field : spec.fields())
            sb.append(indent(FIELD_CODER.code(field), spec.isNested()));

        // Blank line after fields if any
        if (!spec.fields().isEmpty() && !spec.methods().isEmpty())
            sb.append("\n");

        // Methods
        for (MethodCodingInfo method : spec.methods())
            sb.append(indent(METHOD_CODER.code(method), spec.isNested()));

        // Nested classes — recursive
        for (ClassCodingInfo nested : spec.nestedClasses())
            sb.append(indent(code(nested), spec.isNested()));

        sb.append(prefix).append("}\n");
        if (spec.isNested()) sb.append("\n");

        return sb.toString();
    }

    /**
     * Indents every line by one extra level when inside a nested class.
     * Top-level classes don't need extra indentation.
     */
    private static String indent(String src, boolean nested) {
        if (!nested || src.isBlank()) return src;
        return src.lines()
                .map(line -> line.isBlank() ? line : INDENT + line)
                .collect(java.util.stream.Collectors.joining("\n", "", "\n"));
    }
}
