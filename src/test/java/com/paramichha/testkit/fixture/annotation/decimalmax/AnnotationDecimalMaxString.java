package com.paramichha.testkit.fixture.annotation.decimalmax;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationDecimalMaxString(
        @DecimalMax("999.99") String value
) {}
