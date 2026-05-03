package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedStringTemporal(
        @NotBlank @Email String email,
        @NotNull @Past java.time.LocalDate dateOfBirth,
        @NotBlank String name,
        @NotNull @Future java.time.LocalDate expiryDate
) {}
