package com.paramichha.testkit.fixture.temporal.zoneddatetime;

import com.paramichha.testkit.Testable;
import java.time.ZonedDateTime;

@Testable
public record TemporalZonedDateTimeMany(
        ZonedDateTime value1,
        ZonedDateTime value2,
        ZonedDateTime value3
) {}
