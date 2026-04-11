package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

import java.time.*;
import java.util.Date;

/**
 * Produces temporal values for all standard Java date/time types.
 */
final class TemporalTypeShaper implements TypeShaper {

    static final TemporalTypeShaper INSTANCE = new TemporalTypeShaper();

    private TemporalTypeShaper() {
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Instant.class
                || type == LocalDate.class
                || type == LocalDateTime.class
                || type == LocalTime.class
                || type == ZonedDateTime.class
                || type == OffsetDateTime.class
                || type == Date.class
                || type == Year.class
                || type == YearMonth.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return TemporalShaper.shape(field, target);
    }
}
