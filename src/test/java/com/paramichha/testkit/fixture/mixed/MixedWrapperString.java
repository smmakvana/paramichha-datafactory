package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedWrapperString(
        @NotNull Integer age,
        @NotBlank @Email String email,
        @NotNull Long id,
        @NotBlank String name
) {}
