package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNegativeOrZeroMin(
        @NegativeOrZero @Min(-50) Integer delta
) {}
