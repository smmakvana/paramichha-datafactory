package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Double} / {@code double} values. Obtained via {@link DataFactory#doubles()}. */
public interface DoubleValue extends FieldValue<Double> {
    DoubleValue positive();

    DoubleValue negative();

    @Override
    DoubleValue with(Class<? extends Annotation> annotation);

    @Override
    DoubleValue with(String annotation);

    double toDouble();
}
