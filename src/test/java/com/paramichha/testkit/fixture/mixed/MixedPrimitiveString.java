package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedPrimitiveString(
        int count,
        String name,
        int age,
        String email
) {}
