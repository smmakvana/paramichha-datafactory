package com.paramichha.testkit.fixture.strategy.allargs;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public class StrategyAllArgsAnnotated {
    @NotBlank private final String name;
    @NotNull @Min(18) private final Integer age;
    @NotBlank @Email private final String email;

    public StrategyAllArgsAnnotated(String name, Integer age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public String getName() { return name; }
    public Integer getAge() { return age; }
    public String getEmail() { return email; }
}
