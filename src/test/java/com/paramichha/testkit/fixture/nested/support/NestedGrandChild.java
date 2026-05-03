package com.paramichha.testkit.fixture.nested.support;

import jakarta.validation.constraints.*;

public record NestedGrandChild(
        @NotBlank String line1,
        @NotBlank String line2,
        @NotBlank String city
) {}
