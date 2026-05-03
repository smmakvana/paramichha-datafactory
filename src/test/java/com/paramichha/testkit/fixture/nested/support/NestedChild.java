package com.paramichha.testkit.fixture.nested.support;

import jakarta.validation.constraints.*;

public record NestedChild(
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank @Size(min = 2, max = 3) String countryCode,
        @NotBlank @Size(min = 3, max = 10) String postcode
) {}
