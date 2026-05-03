package com.paramichha.testkit.fixture.annotation.future;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationFutureMany(
        @Future java.time.LocalDate expiryDate,
        @Future java.time.LocalDateTime scheduledAt,
        @Future java.time.Instant deliverAt
) {}
