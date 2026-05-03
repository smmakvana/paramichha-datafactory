package com.paramichha.testkit.fixture.strategy.record;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record StrategyRecord(
        String name,
        Integer age
) {}
