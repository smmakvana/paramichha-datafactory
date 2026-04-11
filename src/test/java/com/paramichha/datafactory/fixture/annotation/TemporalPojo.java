package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.Date;
public class TemporalPojo {
    @NotNull @Past Instant createdAt;
    @NotNull @Past LocalDate dateOfBirth;
    @NotNull @Past LocalDateTime submittedAt;
    @NotNull @Future ZonedDateTime expiresAt;
    @NotNull @Future OffsetDateTime deliverAt;
    @NotNull @PastOrPresent Date registeredOn;
    @NotNull @Past Year graduationYear;
    @NotNull @FutureOrPresent YearMonth renewalMonth;
    @NotNull LocalTime scheduledTime;
    public TemporalPojo() {}
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }
    public OffsetDateTime getDeliverAt() { return deliverAt; }
    public void setDeliverAt(OffsetDateTime deliverAt) { this.deliverAt = deliverAt; }
    public Date getRegisteredOn() { return registeredOn; }
    public void setRegisteredOn(Date registeredOn) { this.registeredOn = registeredOn; }
    public Year getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Year graduationYear) { this.graduationYear = graduationYear; }
    public YearMonth getRenewalMonth() { return renewalMonth; }
    public void setRenewalMonth(YearMonth renewalMonth) { this.renewalMonth = renewalMonth; }
    public LocalTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }
}
