package com.paramichha.testkit.fixture.annotation.min;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationMinBigInteger(
        @Min(1) java.math.BigInteger value
) {}
