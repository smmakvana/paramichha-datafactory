package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/**
 * Fluent builder for {@code Float} / {@code float} values. Obtained via {@link DataFactory#floats()}.
 */
public interface FloatValue extends FieldValue<Float> {
    FloatValue positive();

    FloatValue negative();

    @Override
    FloatValue with(Class<? extends Annotation> annotation);

    @Override
    FloatValue with(String annotation);

    float toFloat();
}
