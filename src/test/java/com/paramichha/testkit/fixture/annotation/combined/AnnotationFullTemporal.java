package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFullTemporal(
        @NotNull @Past java.time.LocalDate dateOfBirth,
        @NotNull @Future java.time.LocalDate expiryDate,
        @NotNull @PastOrPresent java.time.LocalDateTime lastLogin
) {}
