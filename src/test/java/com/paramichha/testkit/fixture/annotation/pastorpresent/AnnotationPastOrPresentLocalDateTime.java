package com.paramichha.testkit.fixture.annotation.pastorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastOrPresentLocalDateTime(
        @PastOrPresent java.time.LocalDateTime value
) {}
