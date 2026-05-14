package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Metadata for one field — name, type, Jakarta validation annotations.
 * hasIndirectConstraints is true when the field's type itself has constrained fields.
 */
public record FieldMeta(
        String name,
        Class<?> rawType,
        Type genericType,
        List<Annotation> validations,
        boolean required,
        boolean hasIndirectConstraints
) {
    /**
     * Backward-compatible constructor — no indirect constraints.
     */
    public FieldMeta(String name, Class<?> rawType, Type genericType,
                     List<Annotation> validations, boolean required) {
        this(name, rawType, genericType, validations, required, false);
    }
}
