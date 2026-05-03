package com.paramichha.testkit.fixture.temporal.instant;

import com.paramichha.testkit.Testable;
import java.time.Instant;

@Testable
public record TemporalInstant(
        Instant value
) {}
