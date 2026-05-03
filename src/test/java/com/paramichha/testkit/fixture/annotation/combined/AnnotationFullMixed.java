package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFullMixed(
        @NotBlank @Email String email,
        @NotNull @Min(18) @Max(65) Integer age,
        @NotNull @Past java.time.LocalDate dateOfBirth,
        @NotNull @Positive java.math.BigDecimal amount,
        @AssertTrue Boolean termsAccepted
) {}
