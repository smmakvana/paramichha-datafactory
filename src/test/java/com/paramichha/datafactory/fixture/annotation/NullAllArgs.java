package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class NullAllArgs {
    @NotNull String notNullField;
    @NotBlank String notBlankField;
    @NotEmpty String notEmptyString;
    @NotEmpty java.util.List<String> notEmptyList;
    @Null String nullField;
}
