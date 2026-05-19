package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

/**
 * Metadata for a method's return type — type + Jakarta annotations.
 * Passed directly to DataFactory for value generation.
 */
public final class ReflectionReturnTypeInfo implements ReturnTypeInfo {
    private final Class<?> type;
    private final List<Annotation> annotations;

    /**
     *
     */
    public ReflectionReturnTypeInfo(
            Class<?> type,
            List<Annotation> annotations
    ) {
        this.type = type;
        this.annotations = annotations;
    }

    @Override
    public boolean isVoid() {
        return type == void.class || type == Void.class;
    }

    @Override
    public boolean isConstrained() {
        return !annotations.isEmpty();
    }

    @Override
    public boolean isPrimitive() {
        return type.isPrimitive() && !isVoid();
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public List<Annotation> annotations() {
        return annotations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, annotations);
    }

    @Override
    public String toString() {
        return "ReflectionReturnTypeInfo[" +
                "type=" + type + ", " +
                "annotations=" + annotations + ']';
    }

}