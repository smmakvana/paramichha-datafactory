package com.paramichha.testkit.fixture.annotation.asserttrue;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationAssertTruePrimitive(
        @AssertTrue boolean active
) {}
