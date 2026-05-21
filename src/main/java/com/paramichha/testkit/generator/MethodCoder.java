package com.paramichha.testkit.generator;

import java.util.stream.Collectors;

/**
 * Produces source code for a single method.
 *
 * <p>If {@link MethodCodingInfo#isEmpty()} is true and name is blank,
 * emits body as a raw line (e.g. private record declarations).
 * If isEmpty() is true and name is present, emits as a commented-out block.
 */
public final class MethodCoder implements Coder<MethodCodingInfo> {

    private static final String      INDENT      = "    ";
    private static final String      INDENT_BODY = "        ";
    private static final ParameterCoder PARAM_CODER = new ParameterCoder();

    @Override
    public String code(MethodCodingInfo spec) {
        if (spec.isEmpty()) {
            return spec.name().isBlank()
                    ? codeAsRaw(spec)       // private record / raw line
                    : codeAsComment(spec);  // TODO commented block
        }
        return codeLive(spec);
    }

    // ── Live method ───────────────────────────────────────────────────────

    private String codeLive(MethodCodingInfo spec) {
        StringBuilder sb = new StringBuilder();

        for (String ann : spec.annotations())
            sb.append(INDENT).append(ann).append("\n");

        sb.append(INDENT);
        for (String mod : spec.modifiers())
            sb.append(mod).append(" ");
        sb.append(spec.returnType()).append(" ").append(spec.name()).append("(");
        sb.append(params(spec)).append(") {\n");

        for (String line : spec.body().split("\n", -1))
            sb.append(INDENT_BODY).append(line).append("\n");

        sb.append(INDENT).append("}\n\n");
        return sb.toString();
    }

    // ── Commented-out TODO block — matches old generator format ───────────

    private String codeAsComment(MethodCodingInfo spec) {
        StringBuilder sb = new StringBuilder();

        // Annotations — each on its own commented line
        for (String ann : spec.annotations())
            sb.append(INDENT).append("// ").append(ann).append("\n");

        // Signature line
        sb.append(INDENT).append("// ");
        for (String mod : spec.modifiers())
            sb.append(mod).append(" ");
        sb.append(spec.returnType()).append(" ").append(spec.name()).append("(");
        sb.append(params(spec)).append(") {\n");

        // Body lines — 4-space indent inside comment (matches old output)
        for (String line : spec.body().split("\n", -1))
            sb.append(INDENT).append("//     ").append(line).append("\n");

        sb.append(INDENT).append("// }\n\n");
        return sb.toString();
    }

    // ── Raw emit — private record, standalone comment lines etc ────────────

    private String codeAsRaw(MethodCodingInfo spec) {
        StringBuilder sb = new StringBuilder();
        for (String line : spec.body().split("\n", -1))
            sb.append(INDENT).append(line).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    // ── Param helper ──────────────────────────────────────────────────────

    private static String params(MethodCodingInfo spec) {
        return spec.parameters().stream()
                .map(PARAM_CODER::code)
                .collect(Collectors.joining(", "));
    }
}
