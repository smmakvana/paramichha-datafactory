package com.paramichha.datafactory.generation;

import java.time.*;

/**
 * Fixed reference points for temporal value generation.
 *
 * Two anchors:
 *   PAST_*   — fixed date in the past, satisfies @Past constraints
 *   FUTURE_* — fixed date far in the future, satisfies @Future constraints
 *
 * Both are stable across runs — no LocalDate.now() anywhere.
 */
public final class TemporalAnchor {

    // Past anchor — 2020-06-15, safely before any reasonable test run date
    public static final Instant       PAST_INSTANT   = Instant.parse("2020-06-15T00:00:00Z");
    public static final LocalDate     PAST_DATE      = LocalDate.of(2020, 6, 15);
    public static final LocalDateTime PAST_DATE_TIME = LocalDateTime.of(2020, 6, 15, 12, 0, 0);
    public static final Year          PAST_YEAR      = Year.of(2020);
    public static final YearMonth     PAST_YEARMONTH = YearMonth.of(2020, 6);

    // Future anchor — 2099-06-15, safely ahead of any reasonable test run date
    public static final Instant       FUTURE_INSTANT   = Instant.parse("2099-06-15T00:00:00Z");
    public static final LocalDate     FUTURE_DATE      = LocalDate.of(2099, 6, 15);
    public static final LocalDateTime FUTURE_DATE_TIME = LocalDateTime.of(2099, 6, 15, 12, 0, 0);
    public static final Year          FUTURE_YEAR      = Year.of(2099);
    public static final YearMonth     FUTURE_YEARMONTH = YearMonth.of(2099, 6);

    // Neutral anchor — used for NONE direction (no constraint)
    public static final Instant       NEUTRAL_INSTANT   = Instant.parse("2024-01-15T00:00:00Z");
    public static final LocalDate     NEUTRAL_DATE      = LocalDate.of(2024, 1, 15);
    public static final LocalDateTime NEUTRAL_DATE_TIME = LocalDateTime.of(2024, 1, 15, 12, 0, 0);
    public static final Year          NEUTRAL_YEAR      = Year.of(2024);
    public static final YearMonth     NEUTRAL_YEARMONTH = YearMonth.of(2024, 1);

    public static final LocalTime NEUTRAL_TIME = LocalTime.of(12, 0, 0);
    public static final ZoneId    ZONE         = ZoneId.of("UTC");

    private TemporalAnchor() {}
}