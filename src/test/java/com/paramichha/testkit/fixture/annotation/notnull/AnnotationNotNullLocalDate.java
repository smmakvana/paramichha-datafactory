package com.paramichha.testkit.fixture.annotation.notnull;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullLocalDate(
        @NotNull java.time.LocalDate date
) {}
