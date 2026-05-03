package com.paramichha.testkit.fixture.temporal.localdatetime;

import com.paramichha.testkit.Testable;
import java.time.LocalDateTime;

@Testable
public record TemporalLocalDateTimeMany(
        LocalDateTime value1,
        LocalDateTime value2,
        LocalDateTime value3
) {}
