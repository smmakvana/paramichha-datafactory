package com.paramichha.testkit.fixture.annotation.futureorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureOrPresentOffsetDateTime(
        @FutureOrPresent java.time.OffsetDateTime value
) {}
