package com.paramichha.testkit.fixture.annotation.future;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureInstant(
        @Future java.time.Instant value
) {}
