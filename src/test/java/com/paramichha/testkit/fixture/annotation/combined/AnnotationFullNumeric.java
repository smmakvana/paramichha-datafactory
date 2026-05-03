package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFullNumeric(
        @NotNull @Min(18) @Max(65) Integer age,
        @NotNull @Positive Long userId,
        @NotNull @DecimalMin("0.01") java.math.BigDecimal balance
) {}
