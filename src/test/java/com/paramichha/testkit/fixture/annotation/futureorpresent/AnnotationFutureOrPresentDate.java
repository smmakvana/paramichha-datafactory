package com.paramichha.testkit.fixture.annotation.futureorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureOrPresentDate(
        @FutureOrPresent java.util.Date value
) {}
