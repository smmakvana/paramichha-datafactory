package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Metadata for a method's return type — type + Jakarta annotations.
 * Passed directly to DataFactory for value generation.
 */
public record ReturnTypeInfo(
        Class<?> type,
        List<Annotation> annotations
) {
    public boolean isVoid() {
        return type == void.class || type == Void.class;
    }

    public boolean isConstrained() {
        return !annotations.isEmpty();
    }

    public boolean isPrimitive() {
        return type.isPrimitive() && !isVoid();
    }
}