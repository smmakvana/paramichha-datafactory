package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.analyzer.TemporalDirection;
import com.paramichha.datafactory.planner.BoundaryTarget;

import java.time.*;
import java.util.Date;

/**
 * Produces temporal values respecting the field's {@link TemporalDirection}.
 */
final class TemporalShaper {

    private TemporalShaper() {
    }

    static Object shape(FieldConstraints field, BoundaryTarget target) {
        Class<?> type = field.fieldType();
        TemporalDirection d = field.temporal();

        if (type == Instant.class) return shapeInstant(d);
        if (type == LocalDate.class) return shapeLocalDate(d);
        if (type == LocalDateTime.class) return shapeLocalDateTime(d);
        if (type == LocalTime.class) return LocalTime.now();
        if (type == ZonedDateTime.class) return shapeZonedDateTime(d);
        if (type == OffsetDateTime.class) return shapeOffsetDateTime(d);
        if (type == Date.class) return shapeDate(d);
        if (type == Year.class) return shapeYear(d);
        if (type == YearMonth.class) return shapeYearMonth(d);
        return Instant.now();
    }

    private static Instant shapeInstant(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> Instant.now().minusSeconds(86400 * 30L);
            case FUTURE, FUTURE_OR_NOW -> Instant.now().plusSeconds(86400 * 30L);
            default -> Instant.now();
        };
    }

    private static LocalDate shapeLocalDate(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> LocalDate.now().minusDays(30);
            case FUTURE, FUTURE_OR_NOW -> LocalDate.now().plusDays(30);
            default -> LocalDate.now();
        };
    }

    private static LocalDateTime shapeLocalDateTime(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> LocalDateTime.now().minusDays(30);
            case FUTURE, FUTURE_OR_NOW -> LocalDateTime.now().plusDays(30);
            default -> LocalDateTime.now();
        };
    }

    private static ZonedDateTime shapeZonedDateTime(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> ZonedDateTime.now().minusDays(30);
            case FUTURE, FUTURE_OR_NOW -> ZonedDateTime.now().plusDays(30);
            default -> ZonedDateTime.now();
        };
    }

    private static OffsetDateTime shapeOffsetDateTime(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> OffsetDateTime.now().minusDays(30);
            case FUTURE, FUTURE_OR_NOW -> OffsetDateTime.now().plusDays(30);
            default -> OffsetDateTime.now();
        };
    }

    private static Date shapeDate(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> Date.from(Instant.now().minusSeconds(86400 * 30L));
            case FUTURE, FUTURE_OR_NOW -> Date.from(Instant.now().plusSeconds(86400 * 30L));
            default -> new Date();
        };
    }

    private static Year shapeYear(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> Year.now().minusYears(1);
            case FUTURE, FUTURE_OR_NOW -> Year.now().plusYears(1);
            default -> Year.now();
        };
    }

    private static YearMonth shapeYearMonth(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW -> YearMonth.now().minusMonths(1);
            case FUTURE, FUTURE_OR_NOW -> YearMonth.now().plusMonths(1);
            default -> YearMonth.now();
        };
    }
}
