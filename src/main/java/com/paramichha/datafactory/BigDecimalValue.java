package com.paramichha.datafactory;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

/** Fluent builder for {@code BigDecimal} values. Obtained via {@link DataFactory#decimal()}. */
public interface BigDecimalValue extends FieldValue<BigDecimal> {
    BigDecimalValue range(String min, String max);

    BigDecimalValue min(String min);

    BigDecimalValue max(String max);

    BigDecimalValue positive();

    BigDecimalValue negative();

    BigDecimalValue scale(int scale);

    BigDecimalValue digits(int integer, int fraction);

    @Override
    BigDecimalValue with(Class<? extends Annotation> annotation);

    @Override
    BigDecimalValue with(String annotation);

    BigDecimal toBigDecimal();
}
