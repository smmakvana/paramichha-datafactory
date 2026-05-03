package com.paramichha.testkit.fixture.temporal.yearw;

import com.paramichha.testkit.Testable;


@Testable
public record TemporalYearMany(
        java.time.Year value1,
        java.time.Year value2,
        java.time.Year value3
) {}
