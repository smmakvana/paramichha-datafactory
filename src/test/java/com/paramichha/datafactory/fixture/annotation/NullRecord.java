package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
public record NullRecord(@NotNull String notNullField, @NotBlank String notBlankField, @NotEmpty String notEmptyString, @NotEmpty java.util.List<String> notEmptyList, @Null String nullField) {}
