package com.paramichha.testkit.fixture.strategy.setter;

import com.paramichha.testkit.Testable;

@Testable
public class StrategySetter {
    private String name;
    private Integer age;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
