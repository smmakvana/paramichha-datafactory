package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class UserRegistrationLombok {
    @NotBlank @Email String email;
    @NotBlank @Size(min=8) String password;
    @NotNull @Past LocalDate dateOfBirth;
    @NotNull @Positive Integer age;
    @NotNull UserRegistrationRecord.Status status;

    public enum Status{ACTIVE,INACTIVE,PENDING}
}
