package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class UserRegistrationAllArgs {
    @NotBlank @Email String email;
    @NotBlank @Size(min=8) String password;
    @NotNull @Past LocalDate dateOfBirth;
    @NotNull @Positive Integer age;
    @NotNull UserRegistrationRecord.Status status;

    public enum Status{ACTIVE,INACTIVE,PENDING}
}
