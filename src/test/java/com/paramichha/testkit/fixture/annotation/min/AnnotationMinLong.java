package com.paramichha.testkit.fixture.annotation.min;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMinLong(
        @Min(1) Long value
) {}
