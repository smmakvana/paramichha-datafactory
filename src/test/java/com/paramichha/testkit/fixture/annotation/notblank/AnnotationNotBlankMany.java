package com.paramichha.testkit.fixture.annotation.notblank;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotBlankMany(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String email
) {}
