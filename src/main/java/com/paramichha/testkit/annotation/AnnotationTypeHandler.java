package com.paramichha.testkit.annotation;

import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Annotation;

interface AnnotationTypeHandler {
    /**
     * Each handler decides how to parse attributes and build annotation.
     */
    Annotation create(Class<? extends Annotation> annotationClass, @NotNull String rawAttributeString) throws AnnotationParseException;
}