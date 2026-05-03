package com.paramichha.testkit.fixture.annotation.futureorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureOrPresentZonedDateTime(
        @FutureOrPresent java.time.ZonedDateTime value
) {}
