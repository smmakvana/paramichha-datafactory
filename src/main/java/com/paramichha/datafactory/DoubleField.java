package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Double} / {@code double} values. Obtained via {@link DataFactory#doubles()}. */
public interface DoubleField extends FieldBuilder<Double> {
    DoubleField positive();
    DoubleField negative();
    DoubleField with(Class<? extends Annotation> annotation);
    DoubleField with(String annotation);
    double toDouble();
}
