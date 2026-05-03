package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFullString(
        @NotBlank @Email @Size(min = 5, max = 100) String email,
        @NotBlank @Size(min = 2, max = 50) String name,
        @NotBlank @Size(min = 8, max = 20) String password
) {}
