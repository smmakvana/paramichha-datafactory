package com.paramichha.testkit.fixture.annotation.pattern;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPatternString(
        @Pattern(regexp = "^[A-Z]{2,3}$") String code
) {}
