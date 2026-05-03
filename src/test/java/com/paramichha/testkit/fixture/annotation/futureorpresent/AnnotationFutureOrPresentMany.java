package com.paramichha.testkit.fixture.annotation.futureorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureOrPresentMany(
        @FutureOrPresent java.time.LocalDate renewalDate,
        @FutureOrPresent java.time.YearMonth renewalMonth
) {}
