package com.paramichha.testkit.fixture.annotation.pastorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastOrPresentYearMonth(
        @PastOrPresent java.time.YearMonth value
) {}
