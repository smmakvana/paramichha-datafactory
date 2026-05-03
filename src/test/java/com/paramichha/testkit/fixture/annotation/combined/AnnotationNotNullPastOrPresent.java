package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullPastOrPresent(
        @NotNull @PastOrPresent java.time.LocalDate registeredOn
) {}
