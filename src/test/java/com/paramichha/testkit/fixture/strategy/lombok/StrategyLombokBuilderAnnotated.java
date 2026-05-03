package com.paramichha.testkit.fixture.strategy.lombok;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Testable @Value @Builder
public class StrategyLombokBuilderAnnotated {
    @NotBlank String name;
    @NotNull @Min(18) @Max(65) Integer age;
    @NotBlank @Email String email;
}
