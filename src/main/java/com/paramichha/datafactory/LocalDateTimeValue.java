package com.paramichha.datafactory;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

/** Fluent builder for {@code LocalDateTime} values. Obtained via {@link DataFactory#dateTime()}. */
public interface LocalDateTimeValue extends FieldValue<LocalDateTime> {
    LocalDateTimeValue past();

    LocalDateTimeValue pastOrPresent();

    LocalDateTimeValue future();

    LocalDateTimeValue futureOrPresent();

    @Override
    LocalDateTimeValue with(Class<? extends Annotation> annotation);

    @Override
    LocalDateTimeValue with(String annotation);
}
