package com.paramichha.testkit.fixture.annotation.positive;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPositiveInteger(
        @Positive Integer value
) {}
