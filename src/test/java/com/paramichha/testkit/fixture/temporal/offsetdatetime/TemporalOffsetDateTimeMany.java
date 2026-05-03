package com.paramichha.testkit.fixture.temporal.offsetdatetime;

import com.paramichha.testkit.Testable;
import java.time.OffsetDateTime;

@Testable
public record TemporalOffsetDateTimeMany(
        OffsetDateTime value1,
        OffsetDateTime value2,
        OffsetDateTime value3
) {}
