package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedAllFlat(
        int primitiveInt,
        Integer wrappedInt,
        String text,
        boolean flag,
        java.time.LocalDate date,
        java.math.BigDecimal amount,
        java.util.UUID id
) {}
