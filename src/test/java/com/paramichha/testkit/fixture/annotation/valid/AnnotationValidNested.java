package com.paramichha.testkit.fixture.annotation.valid;

import com.paramichha.testkit.Testable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationValidNested(
        @NotBlank String name,
        @Valid Address address
) {
    public record Address(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank @Size(min = 2, max = 3) String countryCode
    ) {}
}
