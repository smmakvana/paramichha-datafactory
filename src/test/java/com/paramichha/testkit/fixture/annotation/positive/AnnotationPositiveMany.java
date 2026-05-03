package com.paramichha.testkit.fixture.annotation.positive;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPositiveMany(
        @Positive Integer count,
        @Positive Long id,
        @Positive java.math.BigDecimal amount
) {}
