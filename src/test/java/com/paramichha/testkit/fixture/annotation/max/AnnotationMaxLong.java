package com.paramichha.testkit.fixture.annotation.max;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMaxLong(
        @Max(100) Long value
) {}
