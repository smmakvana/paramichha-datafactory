package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPositiveOrZeroMax(
        @PositiveOrZero @Max(100) Integer score
) {}
