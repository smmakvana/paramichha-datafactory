package com.paramichha.datafactory.annotation;

/**
 * High-level category of a Jakarta/Hibernate Validator annotation.
 * Used by {@link AnnotationSupport} to group annotations by concern.
 */
public enum AnnotationType {
    NO_ARG,
    ONE_ARG,
    MULTI_ARG,
}
