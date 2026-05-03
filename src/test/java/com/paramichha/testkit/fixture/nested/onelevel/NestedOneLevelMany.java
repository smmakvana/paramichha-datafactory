package com.paramichha.testkit.fixture.nested.onelevel;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.nested.support.NestedChild;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Testable
public record NestedOneLevelMany(
        @NotBlank String name,
        @Valid @jakarta.validation.constraints.NotNull NestedChild homeAddress,
        @Valid NestedChild workAddress
) {}
