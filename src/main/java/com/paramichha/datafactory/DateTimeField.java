package com.paramichha.datafactory;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

/** Fluent builder for {@code LocalDateTime} values. Obtained via {@link DataFactory#dateTime()}. */
public interface DateTimeField extends FieldBuilder<LocalDateTime> {
    DateTimeField past();
    DateTimeField pastOrPresent();
    DateTimeField future();
    DateTimeField futureOrPresent();
    DateTimeField with(Class<? extends Annotation> annotation);
    DateTimeField with(String annotation);
}
