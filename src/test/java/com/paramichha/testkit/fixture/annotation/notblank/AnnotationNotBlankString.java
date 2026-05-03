package com.paramichha.testkit.fixture.annotation.notblank;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotBlankString(
        @NotBlank String value
) {}
