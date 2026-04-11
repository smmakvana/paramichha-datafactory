package com.paramichha.datafactory.fixture.type;
import java.time.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class TemporalsAllArgs {
     Instant instant;
     LocalDate localDate;
     LocalDateTime localDateTime;
     LocalTime localTime;
     ZonedDateTime zonedDateTime;
     OffsetDateTime offsetDateTime;
     Date date;
     Year year;
     YearMonth yearMonth;
}
