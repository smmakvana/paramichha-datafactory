package com.paramichha.testkit.fixture.nested.twolevel;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.nested.support.NestedItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Testable
public record NestedTwoLevelCollection(
        @NotBlank String owner,
        @NotNull @Valid List<NestedItem> items,
        @NotNull @Positive java.math.BigDecimal total
) {}
