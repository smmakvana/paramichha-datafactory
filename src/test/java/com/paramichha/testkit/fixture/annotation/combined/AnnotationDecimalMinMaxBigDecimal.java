package com.paramichha.testkit.fixture.annotation.combined;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationDecimalMinMaxBigDecimal(
        @DecimalMin("0.01") @DecimalMax("999999.99") java.math.BigDecimal amount
) {}
