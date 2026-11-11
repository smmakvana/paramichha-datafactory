package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Long} / {@code long} values. Obtained via {@link DataFactory#longVal()}. */
public interface LongValue extends FieldValue<Long> {
    LongValue range(long min, long max);

    LongValue min(long min);

    LongValue max(long max);

    LongValue positive();

    LongValue negative();

    @Override
    LongValue with(Class<? extends Annotation> annotation);

    @Override
    LongValue with(String annotation);

    long toLong();
}
