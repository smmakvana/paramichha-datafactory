package com.paramichha.testkit.generator;

import java.util.stream.Collectors;

/**
 * Produces source code for a single method.
 *
 * <p>If {@link MethodCodingInfo#isEmpty()} is true, emits the body as a
 * commented-out TODO block — used for reject tests on unconstrained fields.
 */
public final class MethodCoder implements Coder<MethodCodingInfo> {

    private static final String INDENT      = "    ";
    private static final String INDENT_BODY = "        ";
    private static final ParameterCoder PARAM_CODER = new ParameterCoder();

    @Override
    public String code(MethodCodingInfo spec) {
        return spec.isEmpty() ? codeAsComment(spec) : codeLive(spec);
    }

    private String codeLive(MethodCodingInfo spec) {
        StringBuilder sb = new StringBuilder();

        // Annotations
        for (String ann : spec.annotations())
            sb.append(INDENT).append(ann).append("\n");

        // Signature
        sb.append(INDENT);
        for (String mod : spec.modifiers())
            sb.append(mod).append(" ");
        sb.append(spec.returnType()).append(" ").append(spec.name()).append("(");

        String params = spec.parameters().stream()
                .map(PARAM_CODER::code)
                .collect(Collectors.joining(", "));
        sb.append(params).append(") {\n");

        // Body — indent each line
        for (String line : spec.body().split("\n"))
            sb.append(INDENT_BODY).append(line).append("\n");

        sb.append(INDENT).append("}\n\n");
        return sb.toString();
    }

    private String codeAsComment(MethodCodingInfo spec) {
        StringBuilder sb = new StringBuilder();
        // Prefix every line of annotations + signature + body with //
        for (String ann : spec.annotations())
            sb.append(INDENT).append("// ").append(ann).append("\n");

        sb.append(INDENT).append("// ");
        for (String mod : spec.modifiers())
            sb.append(mod).append(" ");
        sb.append(spec.returnType()).append(" ").append(spec.name()).append("(");
        String params = spec.parameters().stream()
                .map(PARAM_CODER::code)
                .collect(Collectors.joining(", "));
        sb.append(params).append(") {\n");

        for (String line : spec.body().split("\n"))
            sb.append(INDENT).append("// ").append(INDENT_BODY).append(line).append("\n");

        sb.append(INDENT).append("// }\n\n");
        return sb.toString();
    }
}
