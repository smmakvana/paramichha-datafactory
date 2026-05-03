package com.paramichha.testkit.fixture.annotation.decimalmin;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationDecimalMinString(
        @DecimalMin("0.01") String value
) {}
