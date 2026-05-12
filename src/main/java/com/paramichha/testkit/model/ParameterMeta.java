package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Metadata for one method parameter.
 */
public record ParameterMeta(
        String name,
        Class<?> type,
        List<Annotation> validations
) {}
