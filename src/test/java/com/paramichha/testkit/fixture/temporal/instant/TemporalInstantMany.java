package com.paramichha.testkit.fixture.temporal.instant;

import com.paramichha.testkit.Testable;
import java.time.Instant;

@Testable
public record TemporalInstantMany(
        Instant value1,
        Instant value2,
        Instant value3
) {}
