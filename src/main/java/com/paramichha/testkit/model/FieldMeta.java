package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Metadata for one field — name, type, Jakarta validation annotations.
 */
public record FieldMeta(
        String name,
        Class<?> rawType,
        Type genericType,
        List<Annotation> validations,
        boolean required
) {}
