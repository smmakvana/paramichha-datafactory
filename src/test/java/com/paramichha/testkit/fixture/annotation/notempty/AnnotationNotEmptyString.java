package com.paramichha.testkit.fixture.annotation.notempty;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotEmptyString(
        @NotEmpty String value
) {}
