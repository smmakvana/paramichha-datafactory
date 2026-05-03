package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotBlankEmailSize(
        @NotBlank @Email @Size(min = 5, max = 100) String email
) {}
