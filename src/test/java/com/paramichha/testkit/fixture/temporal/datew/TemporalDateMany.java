package com.paramichha.testkit.fixture.temporal.datew;

import com.paramichha.testkit.Testable;


@Testable
public record TemporalDateMany(
        java.util.Date value1,
        java.util.Date value2,
        java.util.Date value3
) {}
