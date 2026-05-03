package com.paramichha.testkit.fixture.annotation.max;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMaxBigDecimal(
        @Max(100) java.math.BigDecimal value
) {}
