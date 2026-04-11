package com.paramichha.datafactory;

import java.lang.annotation.Annotation;
import java.time.LocalDate;

/** Fluent builder for {@code LocalDate} values. Obtained via {@link DataFactory#date()}. */
public interface DateField extends FieldBuilder<LocalDate> {
    DateField past();
    DateField pastOrPresent();
    DateField future();
    DateField futureOrPresent();
    DateField with(Class<? extends Annotation> annotation);
    DateField with(String annotation);
}
