package com.paramichha.testkit.fixture.annotation.notnull;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullEnum(
        @NotNull com.paramichha.testkit.fixture.wrapper.enumw.FixtureStatus status
) {}
