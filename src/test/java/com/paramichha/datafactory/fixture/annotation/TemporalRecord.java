package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.Date;
public record TemporalRecord(@NotNull @Past Instant createdAt, @NotNull @Past LocalDate dateOfBirth, @NotNull @Past LocalDateTime submittedAt, @NotNull @Future ZonedDateTime expiresAt, @NotNull @Future OffsetDateTime deliverAt, @NotNull @PastOrPresent Date registeredOn, @NotNull @Past Year graduationYear, @NotNull @FutureOrPresent YearMonth renewalMonth, @NotNull LocalTime scheduledTime) {}
