package com.paramichha.testkit.fixture.annotation.negative;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNegativeLong(
        @Negative Long value
) {}
