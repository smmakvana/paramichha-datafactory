package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedPrimitiveTemporal(
        int year,
        java.time.LocalDate date,
        long epochSeconds,
        java.time.Instant instant
) {}
