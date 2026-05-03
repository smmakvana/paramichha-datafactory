package com.paramichha.testkit.fixture.annotation.digits;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationDigitsMany(
        @Digits(integer = 8, fraction = 2) java.math.BigDecimal price,
        @Digits(integer = 4, fraction = 0) String pin
) {}
