package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public final class ReflectionFieldInfo {
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

    public boolean isConstrained() {
        return !validations.isEmpty() || hasIndirectConstraints;
    }

    public String name() {
        return name;
    }

    public Class<?> rawType() {
        return rawType;
    }

    public Type genericType() {
        return genericType;
    }

    public List<Annotation> validations() {
        return validations;
    }

    public boolean required() {
        return required;
    }

    public boolean hasIndirectConstraints() {
        return hasIndirectConstraints;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReflectionFieldInfo) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.rawType, that.rawType) &&
                Objects.equals(this.genericType, that.genericType) &&
                Objects.equals(this.validations, that.validations) &&
                this.required == that.required &&
                this.hasIndirectConstraints == that.hasIndirectConstraints;
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