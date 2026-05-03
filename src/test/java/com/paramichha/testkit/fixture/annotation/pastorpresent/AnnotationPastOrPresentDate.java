package com.paramichha.testkit.fixture.annotation.pastorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastOrPresentDate(
        @PastOrPresent java.util.Date value
) {}
