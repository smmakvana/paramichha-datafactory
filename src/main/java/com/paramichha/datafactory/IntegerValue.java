package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Integer} / {@code int} values. Obtained via {@link DataFactory#integer()}. */
public interface IntegerValue extends FieldValue<Integer> {
    IntegerValue range(int min, int max);
    IntegerValue min(int min);
    IntegerValue max(int max);
    IntegerValue positive();
    IntegerValue negative();
    IntegerValue with(Class<? extends Annotation> annotation);
    IntegerValue with(String annotation);
    int toInt();
}
