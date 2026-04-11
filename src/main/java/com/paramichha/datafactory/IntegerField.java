package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Integer} / {@code int} values. Obtained via {@link DataFactory#integer()}. */
public interface IntegerField extends FieldBuilder<Integer> {
    IntegerField range(int min, int max);
    IntegerField min(int min);
    IntegerField max(int max);
    IntegerField positive();
    IntegerField negative();
    IntegerField with(Class<? extends Annotation> annotation);
    IntegerField with(String annotation);
    int toInt();
}
