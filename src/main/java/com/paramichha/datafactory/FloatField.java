package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/**
 * Fluent builder for {@code Float} / {@code float} values. Obtained via {@link DataFactory#floats()}.
 */
public interface FloatField extends FieldBuilder<Float> {
    FloatField positive();

    FloatField negative();

    FloatField with(Class<? extends Annotation> annotation);

    FloatField with(String annotation);

    float toFloat();
}
