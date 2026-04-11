package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public class UserRegistrationPojo {
    @NotBlank @Email String email;
    @NotBlank @Size(min=8) String password;
    @NotNull @Past LocalDate dateOfBirth;
    @NotNull @Positive Integer age;
    @NotNull UserRegistrationRecord.Status status;
    public UserRegistrationPojo() {}
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public UserRegistrationRecord.Status getStatus() { return status; }
    public void setStatus(UserRegistrationRecord.Status status) { this.status = status; }

    public enum Status{ACTIVE,INACTIVE,PENDING}
}
