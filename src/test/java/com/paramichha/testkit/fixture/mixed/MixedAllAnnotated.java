package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedAllAnnotated(
        @Min(18) @Max(65) int age,
        @NotNull @Positive Integer count,
        @NotBlank @Email String email,
        @AssertTrue boolean active,
        @NotNull @Past java.time.LocalDate dob,
        @NotNull @Positive java.math.BigDecimal balance
) {}
