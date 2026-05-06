package com.paramichha.datafactory.annotation;

/**
 * High-level category of a Jakarta/Hibernate Validator annotation.
 * Used by {@link AnnotationLookup} to group annotations by concern.
 */
public enum ArgumentType {
    NO_ARG,
    ONE_ARG,
    MULTI_ARG,
}
