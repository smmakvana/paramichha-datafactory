package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import java.time.*;
import java.util.Date;

/** Produces temporal values for all standard Java date/time types. */
final class TemporalDataCreator implements DataCreator {
    static final TemporalDataCreator INSTANCE = new TemporalDataCreator();
    private TemporalDataCreator() {}

    @Override public boolean supports(Class<?> type) {
        return type == Instant.class || type == LocalDate.class || type == LocalDateTime.class
            || type == LocalTime.class || type == ZonedDateTime.class || type == OffsetDateTime.class
            || type == Date.class || type == Year.class || type == YearMonth.class;
    }

    @Override public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        return TemporalShaper.shape(field, target);
    }
}
