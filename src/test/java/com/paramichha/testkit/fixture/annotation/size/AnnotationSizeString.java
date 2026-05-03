package com.paramichha.testkit.fixture.annotation.size;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationSizeString(
        @Size(min = 2, max = 50) String value
) {}
