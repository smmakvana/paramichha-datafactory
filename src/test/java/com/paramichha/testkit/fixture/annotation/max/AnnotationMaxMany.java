package com.paramichha.testkit.fixture.annotation.max;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMaxMany(
        @Max(120) Integer age,
        @Max(9999) Long id,
        @Max(999) java.math.BigDecimal amount
) {}
