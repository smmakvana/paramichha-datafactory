package com.paramichha.testkit.fixture.annotation.assertfalse;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationAssertFalsePrimitive(
        @AssertFalse boolean deleted
) {}
