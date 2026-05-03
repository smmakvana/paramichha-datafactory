package com.paramichha.testkit.fixture.strategy.setter;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public class StrategySetterAnnotated {
    @NotBlank private String name;
    @NotNull @Min(18) private Integer age;
    @NotBlank @Email private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
