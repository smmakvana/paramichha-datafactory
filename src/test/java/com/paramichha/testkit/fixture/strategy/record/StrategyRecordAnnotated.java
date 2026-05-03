package com.paramichha.testkit.fixture.strategy.record;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record StrategyRecordAnnotated(
        @NotBlank String name,
        @NotNull @Min(18) Integer age,
        @NotBlank @Email String email
) {}
