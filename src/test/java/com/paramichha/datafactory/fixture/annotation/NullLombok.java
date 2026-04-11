package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class NullLombok {
    @NotNull String notNullField;
    @NotBlank String notBlankField;
    @NotEmpty String notEmptyString;
    @NotEmpty java.util.List<String> notEmptyList;
    @Null String nullField;
}
