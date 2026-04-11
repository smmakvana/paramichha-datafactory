package com.paramichha.datafactory.fixtures;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.time.*;
import java.util.Date;

/**
 * Fixture 6 — temporal types with @Past, @Future, @PastOrPresent, @FutureOrPresent, @NotNull.
 * Exercises all TemporalDirection values, all temporal invalid cases (notPast / notFuture),
 * and all TemporalShaper type branches with direction.
 */
@Value
@Builder
public class AnnotatedTemporalRequest {
    @NotNull
    @Past
    Instant createdAt;
    @NotNull
    @Past
    LocalDate dateOfBirth;
    @NotNull
    @Past
    LocalDateTime submittedAt;
    @NotNull
    LocalTime scheduledTime;
    @NotNull
    @Future
    ZonedDateTime expiresAt;
    @NotNull
    @Future
    OffsetDateTime deliverAt;
    @NotNull
    @PastOrPresent
    Date registeredOn;
    @NotNull
    @Past
    Year graduationYear;
    @NotNull
    @FutureOrPresent
    YearMonth renewalMonth;
}
