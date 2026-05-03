package com.paramichha.testkit.examples;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Example annotated class used by {@link GenerateTestForClass}.
 * Replace this with your own class when using GenerateTestForClass.
 */
@Value
@Builder
public class ExampleRequest {

    @NotBlank
    @jakarta.validation.constraints.Email
    @Size(max = 100)
    String email;

    @NotBlank
    @Size(min = 2, max = 50)
    String fullName;

    @NotNull
    @Min(18)
    @Max(120)
    Integer age;

    @NotNull
    @Past
    LocalDate dateOfBirth;

    @NotNull
    @Positive
    BigDecimal accountBalance;
}
