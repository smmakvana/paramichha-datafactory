package com.paramichha.testkit.fixture.annotation.past;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastMany(
        @Past java.time.LocalDate dateOfBirth,
        @Past java.time.LocalDateTime submittedAt,
        @Past java.time.Instant createdAt
) {}
