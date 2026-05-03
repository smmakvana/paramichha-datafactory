package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMinMaxMany(
        @Min(0) @Max(100) Integer score,
        @Min(18) @Max(65) Integer age,
        @Min(1) @Max(999) Long ref
) {}
