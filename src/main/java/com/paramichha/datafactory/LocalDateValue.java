package com.paramichha.datafactory;

import java.lang.annotation.Annotation;
import java.time.LocalDate;

/** Fluent builder for {@code LocalDate} values. Obtained via {@link DataFactory#date()}. */
public interface LocalDateValue extends FieldValue<LocalDate> {
    LocalDateValue past();
    LocalDateValue pastOrPresent();
    LocalDateValue future();
    LocalDateValue futureOrPresent();
    LocalDateValue with(Class<? extends Annotation> annotation);
    LocalDateValue with(String annotation);
}
