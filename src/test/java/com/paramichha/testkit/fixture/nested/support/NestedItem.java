package com.paramichha.testkit.fixture.nested.support;

import jakarta.validation.constraints.*;

public record NestedItem(
        @NotBlank String name,
        @NotNull @Min(1) Integer quantity,
        @NotNull @Positive java.math.BigDecimal price
) {}
