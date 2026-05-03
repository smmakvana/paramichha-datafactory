package com.paramichha.testkit.fixture.annotation.decimalmin;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Testable
public record AnnotationDecimalMinDouble(
        @DecimalMin("0.01") BigDecimal value
) {}
