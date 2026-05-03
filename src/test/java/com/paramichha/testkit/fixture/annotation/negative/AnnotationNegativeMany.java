package com.paramichha.testkit.fixture.annotation.negative;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNegativeMany(
        @Negative Integer offset,
        @Negative Long adjustment
) {}
