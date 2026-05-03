package com.paramichha.testkit.fixture.annotation.decimalmax;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Testable
public record AnnotationDecimalMaxDouble(
        @DecimalMax("999.99") BigDecimal value
) {}
