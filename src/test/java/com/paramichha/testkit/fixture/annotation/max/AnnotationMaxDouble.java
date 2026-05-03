package com.paramichha.testkit.fixture.annotation.max;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMaxDouble(
        @Max(100) Double value
) {}
