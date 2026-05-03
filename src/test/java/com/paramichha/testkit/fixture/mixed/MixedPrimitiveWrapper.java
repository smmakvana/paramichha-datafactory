package com.paramichha.testkit.fixture.mixed;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record MixedPrimitiveWrapper(
        int primitiveAge,
        Integer wrapperAge,
        long primitiveId,
        Long wrapperId
) {}
