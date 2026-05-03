package com.paramichha.testkit.fixture.annotation.min;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMinMany(
        @Min(0) Integer age,
        @Min(1) Long id,
        @Min(0) java.math.BigDecimal amount
) {}
