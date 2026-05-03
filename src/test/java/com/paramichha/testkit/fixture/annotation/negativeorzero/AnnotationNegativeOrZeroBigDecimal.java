package com.paramichha.testkit.fixture.annotation.negativeorzero;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNegativeOrZeroBigDecimal(
        @NegativeOrZero java.math.BigDecimal value
) {}
