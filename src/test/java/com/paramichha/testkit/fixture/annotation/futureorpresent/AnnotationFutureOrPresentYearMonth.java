package com.paramichha.testkit.fixture.annotation.futureorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureOrPresentYearMonth(
        @FutureOrPresent java.time.YearMonth value
) {}
