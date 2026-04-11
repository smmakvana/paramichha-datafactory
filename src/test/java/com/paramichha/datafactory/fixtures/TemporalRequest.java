package com.paramichha.datafactory.fixtures;

import lombok.Builder;
import lombok.Value;

import java.time.*;
import java.util.Date;

/**
 * Fixture 3 — all common temporal types, no annotations.
 * Exercises TemporalShaper NONE direction for every type,
 * toSourceCode for all temporal branches.
 */
@Value
@Builder
public class TemporalRequest {
    Instant instant;
    LocalDate localDate;
    LocalDateTime localDateTime;
    LocalTime localTime;
    ZonedDateTime zonedDateTime;
    OffsetDateTime offsetDateTime;
    Date date;
    Year year;
    YearMonth yearMonth;
}
