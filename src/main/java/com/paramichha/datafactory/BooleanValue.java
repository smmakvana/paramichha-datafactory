package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Boolean} / {@code boolean} values. Obtained via {@link DataFactory#bool()}. */
public interface BooleanValue extends FieldValue<Boolean> {
    BooleanValue assertTrue();

    BooleanValue assertFalse();

    BooleanValue with(Class<? extends Annotation> annotation);

    BooleanValue with(String annotation);
}
