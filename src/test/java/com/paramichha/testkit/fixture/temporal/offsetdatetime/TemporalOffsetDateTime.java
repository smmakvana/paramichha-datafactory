package com.paramichha.testkit.fixture.temporal.offsetdatetime;

import com.paramichha.testkit.Testable;
import java.time.OffsetDateTime;

@Testable
public record TemporalOffsetDateTime(
        OffsetDateTime value
) {}
