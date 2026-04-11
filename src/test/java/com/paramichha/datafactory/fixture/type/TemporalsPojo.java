package com.paramichha.datafactory.fixture.type;
import java.time.*;
import java.util.Date;
public class TemporalsPojo {
     Instant instant;
     LocalDate localDate;
     LocalDateTime localDateTime;
     LocalTime localTime;
     ZonedDateTime zonedDateTime;
     OffsetDateTime offsetDateTime;
     Date date;
     Year year;
     YearMonth yearMonth;
    public TemporalsPojo() {}
    public Instant getInstant() { return instant; }
    public void setInstant(Instant instant) { this.instant = instant; }
    public LocalDate getLocalDate() { return localDate; }
    public void setLocalDate(LocalDate localDate) { this.localDate = localDate; }
    public LocalDateTime getLocalDateTime() { return localDateTime; }
    public void setLocalDateTime(LocalDateTime localDateTime) { this.localDateTime = localDateTime; }
    public LocalTime getLocalTime() { return localTime; }
    public void setLocalTime(LocalTime localTime) { this.localTime = localTime; }
    public ZonedDateTime getZonedDateTime() { return zonedDateTime; }
    public void setZonedDateTime(ZonedDateTime zonedDateTime) { this.zonedDateTime = zonedDateTime; }
    public OffsetDateTime getOffsetDateTime() { return offsetDateTime; }
    public void setOffsetDateTime(OffsetDateTime offsetDateTime) { this.offsetDateTime = offsetDateTime; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public Year getYear() { return year; }
    public void setYear(Year year) { this.year = year; }
    public YearMonth getYearMonth() { return yearMonth; }
    public void setYearMonth(YearMonth yearMonth) { this.yearMonth = yearMonth; }
}
