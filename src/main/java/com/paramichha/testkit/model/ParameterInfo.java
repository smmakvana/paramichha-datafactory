package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

public final class ParameterInfo {
    private final String name;
    private final Class<?> type;
    private final List<Annotation> validations;
    private final boolean hasIndirectConstraints;

    public ParameterInfo(
            String name,
            Class<?> type,
            List<Annotation> validations,
            boolean hasIndirectConstraints
    ) {
        this.name = name;
        this.type = type;
        this.validations = validations;
        this.hasIndirectConstraints = hasIndirectConstraints;
    }

    public ParameterInfo(String name, Class<?> type, List<Annotation> validations) {
        this(name, type, validations, false);
    }

    public boolean isConstrained() {
        return !validations.isEmpty() || hasIndirectConstraints;
    }

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
    }

    public List<Annotation> validations() {
        return validations;
    }

    public boolean hasIndirectConstraints() {
        return hasIndirectConstraints;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParameterInfo) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.validations, that.validations) &&
                this.hasIndirectConstraints == that.hasIndirectConstraints;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, validations, hasIndirectConstraints);
    }

    @Override
    public String toString() {
        return "ParameterInfo[" +
                "name=" + name + ", " +
                "type=" + type + ", " +
                "validations=" + validations + ", " +
                "hasIndirectConstraints=" + hasIndirectConstraints + ']';
    }

}