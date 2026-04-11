package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.TemporalDirection;

import java.time.*;
import java.util.Date;

/**
 * Produces temporal values respecting the field's {@link TemporalDirection}.
 * Uses {@link TemporalAnchor} — fixed dates, stable across runs.
 */
public final class TemporalShaper {

    private TemporalShaper() {}

    static Object shape(FieldConstraints field, @SuppressWarnings("unused") BoundaryTarget target) {
        Class<?> type = field.fieldType();
        TemporalDirection d = field.temporal();

        if (type == Instant.class)        return shapeInstant(d);
        if (type == LocalDate.class)      return shapeLocalDate(d);
        if (type == LocalDateTime.class)  return shapeLocalDateTime(d);
        if (type == LocalTime.class)      return TemporalAnchor.NEUTRAL_TIME;
        if (type == ZonedDateTime.class)  return shapeZonedDateTime(d);
        if (type == OffsetDateTime.class) return shapeOffsetDateTime(d);
        if (type == Date.class)           return shapeDate(d);
        if (type == Year.class)           return shapeYear(d);
        if (type == YearMonth.class)      return shapeYearMonth(d);
        return TemporalAnchor.NEUTRAL_INSTANT;
    }

    private static Instant shapeInstant(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> TemporalAnchor.PAST_INSTANT;
            case FUTURE, FUTURE_OR_NOW     -> TemporalAnchor.FUTURE_INSTANT;
            default                        -> TemporalAnchor.NEUTRAL_INSTANT;
        };
    }

    private static LocalDate shapeLocalDate(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> TemporalAnchor.PAST_DATE;
            case FUTURE, FUTURE_OR_NOW     -> TemporalAnchor.FUTURE_DATE;
            default                        -> TemporalAnchor.NEUTRAL_DATE;
        };
    }

    private static LocalDateTime shapeLocalDateTime(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> TemporalAnchor.PAST_DATE_TIME;
            case FUTURE, FUTURE_OR_NOW     -> TemporalAnchor.FUTURE_DATE_TIME;
            default                        -> TemporalAnchor.NEUTRAL_DATE_TIME;
        };
    }

    private static ZonedDateTime shapeZonedDateTime(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> TemporalAnchor.PAST_DATE_TIME.atZone(TemporalAnchor.ZONE);
            case FUTURE, FUTURE_OR_NOW     -> TemporalAnchor.FUTURE_DATE_TIME.atZone(TemporalAnchor.ZONE);
            default                        -> TemporalAnchor.NEUTRAL_DATE_TIME.atZone(TemporalAnchor.ZONE);
        };
    }

    private static OffsetDateTime shapeOffsetDateTime(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> TemporalAnchor.PAST_DATE_TIME.atOffset(ZoneOffset.UTC);
            case FUTURE, FUTURE_OR_NOW     -> TemporalAnchor.FUTURE_DATE_TIME.atOffset(ZoneOffset.UTC);
            default                        -> TemporalAnchor.NEUTRAL_DATE_TIME.atOffset(ZoneOffset.UTC);
        };
    }

    private static Date shapeDate(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> Date.from(TemporalAnchor.PAST_INSTANT);
            case FUTURE, FUTURE_OR_NOW     -> Date.from(TemporalAnchor.FUTURE_INSTANT);
            default                        -> Date.from(TemporalAnchor.NEUTRAL_INSTANT);
        };
    }

    private static Year shapeYear(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> TemporalAnchor.PAST_YEAR;
            case FUTURE, FUTURE_OR_NOW     -> TemporalAnchor.FUTURE_YEAR;
            default                        -> TemporalAnchor.NEUTRAL_YEAR;
        };
    }

    private static YearMonth shapeYearMonth(TemporalDirection d) {
        return switch (d) {
            case PAST, PAST_OR_NOW         -> TemporalAnchor.PAST_YEARMONTH;
            case FUTURE, FUTURE_OR_NOW     -> TemporalAnchor.FUTURE_YEARMONTH;
            default                        -> TemporalAnchor.NEUTRAL_YEARMONTH;
        };
    }
}