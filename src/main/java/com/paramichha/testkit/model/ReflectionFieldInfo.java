package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public final class ReflectionFieldInfo implements FieldInfo {
    private final String name;
    private final Class<?> rawType;
    private final Type genericType;
    private final List<Annotation> validations;
    private final boolean required;
    private final boolean hasIndirectConstraints;

    public ReflectionFieldInfo(
            String name,
            Class<?> rawType,
            Type genericType,
            List<Annotation> validations,
            boolean required,
            boolean hasIndirectConstraints
    ) {
        this.name = name;
        this.rawType = rawType;
        this.genericType = genericType;
        this.validations = validations;
        this.required = required;
        this.hasIndirectConstraints = hasIndirectConstraints;
    }

    public ReflectionFieldInfo(String name, Class<?> rawType, Type genericType,
                               List<Annotation> validations, boolean required) {
        this(name, rawType, genericType, validations, required, false);
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
    public Class<?> rawType() {
        return rawType;
    }

    @Override
    public Type genericType() {
        return genericType;
    }

    @Override
    public List<Annotation> validations() {
        return validations;
    }

    @Override
    public boolean required() {
        return required;
    }

    @Override
    public boolean hasIndirectConstraints() {
        return hasIndirectConstraints;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rawType, genericType, validations, required, hasIndirectConstraints);
    }

    @Override
    public String toString() {
        return "ReflectionFieldInfo[" +
                "name=" + name + ", " +
                "rawType=" + rawType + ", " +
                "genericType=" + genericType + ", " +
                "validations=" + validations + ", " +
                "required=" + required + ", " +
                "hasIndirectConstraints=" + hasIndirectConstraints + ']';
    }

}