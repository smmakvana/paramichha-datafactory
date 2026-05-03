package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedWrapperTemporal(
        @NotNull @Positive java.math.BigDecimal amount,
        @NotNull @Past java.time.LocalDate dateOfBirth,
        @NotNull @Future java.time.LocalDate expiryDate
) {}
