package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Short} / {@code short} values. Obtained via {@link DataFactory#shorts()}. */
public interface ShortValue extends FieldValue<Short> {
    ShortValue range(short min, short max);

    ShortValue min(short min);

    ShortValue max(short max);

    ShortValue positive();

    ShortValue negative();

    ShortValue with(Class<? extends Annotation> annotation);

    ShortValue with(String annotation);

    short toShort();
}
