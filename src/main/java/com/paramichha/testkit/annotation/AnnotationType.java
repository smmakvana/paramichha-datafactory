package com.paramichha.testkit.annotation;

/**
 * High-level category of a Jakarta/Hibernate Validator annotation.
 * Used by {@link AnnotationSupport} to group annotations by concern.
 */
enum AnnotationType {
    NO_ARG,
    ONE_ARG,
    MULTI_ARG,
}
