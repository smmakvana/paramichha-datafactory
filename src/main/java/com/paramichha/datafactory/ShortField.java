package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Short} / {@code short} values. Obtained via {@link DataFactory#shorts()}. */
public interface ShortField extends FieldBuilder<Short> {
    ShortField range(short min, short max);
    ShortField min(short min);
    ShortField max(short max);
    ShortField positive();
    ShortField negative();
    ShortField with(Class<? extends Annotation> annotation);
    ShortField with(String annotation);
    short toShort();
}
