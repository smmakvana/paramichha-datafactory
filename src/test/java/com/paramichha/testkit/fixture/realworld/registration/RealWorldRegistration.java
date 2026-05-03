package com.paramichha.testkit.fixture.realworld.registration;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Testable
public record RealWorldRegistration(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 2, max = 50) String firstName,
        @NotBlank @Size(min = 2, max = 50) String lastName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank @Size(min = 7, max = 20) String phone,
        @NotBlank @Size(min = 2, max = 3) String countryCode
) {}
