package com.paramichha.testkit.generator;

/**
 * Produces source code for a single method parameter.
 *
 * <pre>
 * new ParameterCoder().code(info) → "@NotNull String email"
 * </pre>
 */
public final class ParameterCoder implements Coder<ParameterCodingInfo> {

    @Override
    public String code(ParameterCodingInfo spec) {
        StringBuilder sb = new StringBuilder();
        for (String ann : spec.annotations())
            sb.append(ann).append(" ");
        sb.append(spec.typeName()).append(" ").append(spec.name());
        return sb.toString();
    }
}
