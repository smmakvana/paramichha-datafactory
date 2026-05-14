package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

public record FieldMeta(
        String name,
        Class<?> rawType,
        Type genericType,
        List<Annotation> validations,
        boolean required,
        boolean hasIndirectConstraints
) {
    public FieldMeta(String name, Class<?> rawType, Type genericType,
                     List<Annotation> validations, boolean required) {
        this(name, rawType, genericType, validations, required, false);
    }

    public boolean isConstrained() {
        return !validations.isEmpty() || hasIndirectConstraints;
    }
}