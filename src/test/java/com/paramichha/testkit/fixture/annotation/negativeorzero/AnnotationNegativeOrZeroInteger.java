package com.paramichha.testkit.fixture.annotation.negativeorzero;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNegativeOrZeroInteger(
        @NegativeOrZero Integer value
) {}
