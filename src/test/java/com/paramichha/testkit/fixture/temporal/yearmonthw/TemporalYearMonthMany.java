package com.paramichha.testkit.fixture.temporal.yearmonthw;

import com.paramichha.testkit.Testable;


@Testable
public record TemporalYearMonthMany(
        java.time.YearMonth value1,
        java.time.YearMonth value2,
        java.time.YearMonth value3
) {}
