package com.paramichha.datafactory.annotation;

import com.paramichha.datafactory.AnnotationParseException;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Annotation;

public interface AnnotationTypeHandler {
    /**
     * Each handler decides how to parse attributes and build annotation.
     */
    Annotation create(Class<? extends Annotation> annotationClass, @NotNull String rawAttributeString) throws AnnotationParseException;
}