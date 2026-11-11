package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.builder.FieldValueBuilder;
import com.paramichha.datafactory.builder.ValueBuilderFactory;
import jakarta.validation.constraints.*;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Temporal fallbacks for Date, Year and YearMonth. */
@DisplayName("DefaultFieldBuilder — temporal fallbacks")
class TemporalFallbackTest {

    @Nested
    @DisplayName("futureTemporal — Date, Year, YearMonth")
    class DateYearAndYearMonth {

        @Test
        void datePastNotPastIsDate() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Date.class)).with("@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.constraint().equals("Past")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(Date.class);
        }

        @Test
        void yearPastNotPastIsYear() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Year.class)).with("@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.constraint().equals("Past")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(Year.class);
            assertThat((Year) notPast.get().value()).isGreaterThan(Year.now());
        }

        @Test
        void yearMonth_past_notPast_is_yearmonth() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(YearMonth.class)).with("@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.constraint().equals("Past")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(YearMonth.class);
            assertThat((YearMonth) notPast.get().value()).isGreaterThan(YearMonth.now());
        }

        @Test
        void dateFutureNotFutureIsDate() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Date.class)).with("@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.constraint().equals("Future")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(Date.class);
        }

        @Test
        void yearFutureNotFutureIsYear() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Year.class)).with("@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.constraint().equals("Future")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(Year.class);
            assertThat((Year) notFuture.get().value()).isLessThan(Year.now());
        }

        @Test
        void yearMonth_future_notFuture_is_yearmonth() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(YearMonth.class)).with("@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.constraint().equals("Future")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(YearMonth.class);
            assertThat((YearMonth) notFuture.get().value()).isLessThan(YearMonth.now());
        }
    }
}
