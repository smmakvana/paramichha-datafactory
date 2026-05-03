package com.paramichha.testkit.fixture.collection.listpojo;

import jakarta.validation.constraints.*;

public record CollectionItem(
        @NotBlank String name,
        @Min(0) Integer quantity
) {}
