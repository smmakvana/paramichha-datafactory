package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
public record StringSizeRecord(@NotBlank @Size(min=2,max=50) String username, @NotBlank @Size(min=0,max=200) String notes, @NotBlank @Length(min=5,max=100) String bio, @NotBlank @Digits(integer=5,fraction=0) String accountCode) {}
