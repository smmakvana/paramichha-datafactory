package com.paramichha.testkit.fixture.realworld.registration;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record RealWorldRegistrationMinimal(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
) {}
