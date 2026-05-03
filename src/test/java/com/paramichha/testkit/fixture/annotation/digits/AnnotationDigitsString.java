package com.paramichha.testkit.fixture.annotation.digits;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationDigitsString(
        @Digits(integer = 6, fraction = 0) String accountNumber
) {}
