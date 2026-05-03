package com.paramichha.testkit.fixture.annotation.size;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationSizeStringMany(
        @Size(min = 2, max = 50) String firstName,
        @Size(min = 2, max = 50) String lastName,
        @Size(max = 100) String description
) {}
