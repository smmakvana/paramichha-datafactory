package com.paramichha.testkit.fixture.temporal.zoneddatetime;

import com.paramichha.testkit.Testable;
import java.time.ZonedDateTime;

@Testable
public record TemporalZonedDateTime(
        ZonedDateTime value
) {}
