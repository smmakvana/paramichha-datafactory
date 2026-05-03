package com.paramichha.testkit.fixture.annotation.future;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureYear(
        @Future java.time.Year value
) {}
