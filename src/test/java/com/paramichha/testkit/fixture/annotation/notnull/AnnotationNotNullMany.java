package com.paramichha.testkit.fixture.annotation.notnull;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullMany(
        @NotNull String name,
        @NotNull Integer age,
        @NotNull java.time.LocalDate dob
) {}
