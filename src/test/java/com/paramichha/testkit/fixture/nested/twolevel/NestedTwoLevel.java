package com.paramichha.testkit.fixture.nested.twolevel;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.nested.support.NestedChild;
import com.paramichha.testkit.fixture.nested.support.NestedGrandChild;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Testable
public record NestedTwoLevel(
        @NotBlank String name,
        @Valid NestedChild address,
        @Valid NestedGrandChild detailedAddress
) {}
