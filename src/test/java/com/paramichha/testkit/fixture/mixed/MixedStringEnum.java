package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.wrapper.enumw.FixtureStatus;
import jakarta.validation.constraints.*;

@Testable
public record MixedStringEnum(
        @NotBlank String name,
        @NotNull FixtureStatus status,
        @NotBlank String description
) {}
