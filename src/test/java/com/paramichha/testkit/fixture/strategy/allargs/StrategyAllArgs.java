package com.paramichha.testkit.fixture.strategy.allargs;

import com.paramichha.testkit.Testable;

@Testable
public class StrategyAllArgs {
    private final String name;
    private final Integer age;

    public StrategyAllArgs(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public Integer getAge() { return age; }
}
