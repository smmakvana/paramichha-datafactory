package com.paramichha.testkit.fixture.annotation.assertfalse;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationAssertFalseBoolean(
        @AssertFalse Boolean deleted
) {}
