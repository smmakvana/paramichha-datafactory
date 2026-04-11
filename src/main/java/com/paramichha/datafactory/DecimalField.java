package com.paramichha.datafactory;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

/** Fluent builder for {@code BigDecimal} values. Obtained via {@link DataFactory#decimal()}. */
public interface DecimalField extends FieldBuilder<BigDecimal> {
    DecimalField range(String min, String max);
    DecimalField min(String min);
    DecimalField max(String max);
    DecimalField positive();
    DecimalField negative();
    DecimalField scale(int scale);
    DecimalField digits(int integer, int fraction);
    DecimalField with(Class<? extends Annotation> annotation);
    DecimalField with(String annotation);
    BigDecimal toBigDecimal();
}
