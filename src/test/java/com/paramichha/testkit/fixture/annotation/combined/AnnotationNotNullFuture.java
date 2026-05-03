package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullFuture(
        @NotNull @Future java.time.LocalDate expiryDate
) {}
