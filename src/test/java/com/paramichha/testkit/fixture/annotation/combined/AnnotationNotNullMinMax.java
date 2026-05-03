package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullMinMax(
        @NotNull @Min(18) @Max(120) Integer age
) {}
