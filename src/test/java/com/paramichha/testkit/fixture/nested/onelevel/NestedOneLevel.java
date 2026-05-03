package com.paramichha.testkit.fixture.nested.onelevel;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.nested.support.NestedChild;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Testable
public record NestedOneLevel(
        @NotBlank String name,
        @NotBlank @Email String email,
        @Valid NestedChild address
) {}
