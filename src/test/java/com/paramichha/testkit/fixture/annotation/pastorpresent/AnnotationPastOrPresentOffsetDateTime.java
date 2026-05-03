package com.paramichha.testkit.fixture.annotation.pastorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastOrPresentOffsetDateTime(
        @PastOrPresent java.time.OffsetDateTime value
) {}
