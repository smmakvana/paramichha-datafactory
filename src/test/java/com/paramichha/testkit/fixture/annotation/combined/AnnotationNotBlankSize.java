package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotBlankSize(
        @NotBlank @Size(min = 2, max = 50) String name
) {}
