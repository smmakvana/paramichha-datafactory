package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

public final class ReflectionParameterInfo implements ParameterInfo {
    private final String name;
    private final Class<?> type;
    private final List<Annotation> validations;
    private final boolean hasIndirectConstraints;

    public ReflectionParameterInfo(
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

    public ReflectionParameterInfo(String name, Class<?> type, List<Annotation> validations) {
        this(name, type, validations, false);
    }

    @Override
    public boolean isConstrained() {
        return !validations.isEmpty() || hasIndirectConstraints;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public List<Annotation> validations() {
        return validations;
    }

    @Override
    public boolean hasIndirectConstraints() {
        return hasIndirectConstraints;
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