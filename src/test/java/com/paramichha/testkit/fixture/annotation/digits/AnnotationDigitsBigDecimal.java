package com.paramichha.testkit.fixture.annotation.digits;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationDigitsBigDecimal(
        @Digits(integer = 8, fraction = 2) java.math.BigDecimal price
) {}
