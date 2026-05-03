package com.paramichha.testkit.fixture.annotation.min;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Testable
public record AnnotationMinFloat(
        @Min(1) BigDecimal value
) {}
