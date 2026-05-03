package com.paramichha.testkit.fixture.strategy.lombok;

import com.paramichha.testkit.Testable;
import lombok.Builder;
import lombok.Value;

@Testable @Value @Builder
public class StrategyLombokBuilder {
    String name;
    Integer age;
}
