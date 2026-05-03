package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Double} / {@code double} values. Obtained via {@link DataFactory#doubles()}. */
public interface DoubleValue extends FieldValue<Double> {
    DoubleValue positive();
    DoubleValue negative();
    DoubleValue with(Class<? extends Annotation> annotation);
    DoubleValue with(String annotation);
    double toDouble();
}
