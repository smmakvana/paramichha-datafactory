package com.paramichha.testkit.fixture.annotation.past;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastOffsetDateTime(
        @Past java.time.OffsetDateTime value
) {}
