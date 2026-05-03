package com.paramichha.testkit.fixture.temporal.localdatetime;

import com.paramichha.testkit.Testable;
import java.time.LocalDateTime;

@Testable
public record TemporalLocalDateTime(
        LocalDateTime value
) {}
