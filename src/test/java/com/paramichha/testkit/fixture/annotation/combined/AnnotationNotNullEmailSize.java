package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullEmailSize(
        @NotNull @Email @Size(max = 100) String email
) {}
