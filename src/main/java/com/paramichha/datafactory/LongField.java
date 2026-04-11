package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Long} / {@code long} values. Obtained via {@link DataFactory#longVal()}. */
public interface LongField extends FieldBuilder<Long> {
    LongField range(long min, long max);
    LongField min(long min);
    LongField max(long max);
    LongField positive();
    LongField negative();
    LongField with(Class<? extends Annotation> annotation);
    LongField with(String annotation);
    long toLong();
}
