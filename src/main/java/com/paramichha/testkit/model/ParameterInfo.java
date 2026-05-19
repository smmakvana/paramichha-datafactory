package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

public record ParameterInfo(
        String name,
        Class<?> type,
        List<Annotation> validations,
        boolean hasIndirectConstraints
) {
    public ParameterInfo(String name, Class<?> type, List<Annotation> validations) {
        this(name, type, validations, false);
    }

    public boolean isConstrained() {
        return !validations.isEmpty() || hasIndirectConstraints;
    }
}