package com.paramichha.testkit.fixture.annotation.positiveorzero;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPositiveOrZeroMany(
        @PositiveOrZero Integer score,
        @PositiveOrZero java.math.BigDecimal balance
) {}
