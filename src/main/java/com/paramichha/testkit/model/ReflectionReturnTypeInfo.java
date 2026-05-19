package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

/**
 * Metadata for a method's return type — type + Jakarta annotations.
 * Passed directly to DataFactory for value generation.
 */
public final class ReflectionReturnTypeInfo {
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

    public boolean isVoid() {
        return type == void.class || type == Void.class;
    }

    public boolean isConstrained() {
        return !annotations.isEmpty();
    }

    public boolean isPrimitive() {
        return type.isPrimitive() && !isVoid();
    }

    public Class<?> type() {
        return type;
    }

    public List<Annotation> annotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReflectionReturnTypeInfo) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.annotations, that.annotations);
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