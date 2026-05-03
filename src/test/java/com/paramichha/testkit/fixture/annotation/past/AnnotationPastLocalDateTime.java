package com.paramichha.testkit.fixture.annotation.past;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastLocalDateTime(
        @Past java.time.LocalDateTime value
) {}
