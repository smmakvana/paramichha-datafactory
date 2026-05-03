package com.paramichha.testkit.fixture.annotation.positiveorzero;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPositiveOrZeroFloat(
        @PositiveOrZero Float value
) {}
