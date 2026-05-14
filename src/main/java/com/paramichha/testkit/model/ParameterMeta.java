package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Metadata for one method parameter.
 * hasIndirectConstraints is true when the parameter type has constrained fields.
 */
public record ParameterMeta(
        String name,
        Class<?> type,
        List<Annotation> validations,
        boolean hasIndirectConstraints
) {
    /** Backward-compatible constructor — no indirect constraints. */
    public ParameterMeta(String name, Class<?> type, List<Annotation> validations) {
        this(name, type, validations, false);
    }

    /** True if this parameter has any constraints — direct or indirect. */
    public boolean isConstrained() {
        return !validations.isEmpty() || hasIndirectConstraints;
    }
}