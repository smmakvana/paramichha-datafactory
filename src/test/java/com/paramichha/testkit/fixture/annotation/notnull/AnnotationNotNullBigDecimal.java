package com.paramichha.testkit.fixture.annotation.notnull;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullBigDecimal(
        @NotNull java.math.BigDecimal value
) {}
