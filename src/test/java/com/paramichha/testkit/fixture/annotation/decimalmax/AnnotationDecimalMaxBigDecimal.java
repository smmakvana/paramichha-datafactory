package com.paramichha.testkit.fixture.annotation.decimalmax;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationDecimalMaxBigDecimal(
        @DecimalMax("999.99") java.math.BigDecimal value
) {}
