package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.temporal.localdate.TemporalLocalDate;
import com.paramichha.testkit.fixture.temporal.localdate.TemporalLocalDateMany;
import com.paramichha.testkit.fixture.temporal.localdatetime.TemporalLocalDateTime;
import com.paramichha.testkit.fixture.temporal.localdatetime.TemporalLocalDateTimeMany;
import com.paramichha.testkit.fixture.temporal.localtime.TemporalLocalTime;
import com.paramichha.testkit.fixture.temporal.localtime.TemporalLocalTimeMany;
import com.paramichha.testkit.fixture.temporal.instant.TemporalInstant;
import com.paramichha.testkit.fixture.temporal.instant.TemporalInstantMany;
import com.paramichha.testkit.fixture.temporal.zoneddatetime.TemporalZonedDateTime;
import com.paramichha.testkit.fixture.temporal.zoneddatetime.TemporalZonedDateTimeMany;
import com.paramichha.testkit.fixture.temporal.offsetdatetime.TemporalOffsetDateTime;
import com.paramichha.testkit.fixture.temporal.offsetdatetime.TemporalOffsetDateTimeMany;
import com.paramichha.testkit.fixture.temporal.datew.TemporalDate;
import com.paramichha.testkit.fixture.temporal.datew.TemporalDateMany;
import com.paramichha.testkit.fixture.temporal.yearw.TemporalYear;
import com.paramichha.testkit.fixture.temporal.yearw.TemporalYearMany;
import com.paramichha.testkit.fixture.temporal.yearmonthw.TemporalYearMonth;
import com.paramichha.testkit.fixture.temporal.yearmonthw.TemporalYearMonthMany;

@DisplayName("Phase 3 — Temporal types")
class TemporalPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            TemporalLocalDate.class,
            TemporalLocalDateMany.class,
            TemporalLocalDateTime.class,
            TemporalLocalDateTimeMany.class,
            TemporalLocalTime.class,
            TemporalLocalTimeMany.class,
            TemporalInstant.class,
            TemporalInstantMany.class,
            TemporalZonedDateTime.class,
            TemporalZonedDateTimeMany.class,
            TemporalOffsetDateTime.class,
            TemporalOffsetDateTimeMany.class,
            TemporalDate.class,
            TemporalDateMany.class,
            TemporalYear.class,
            TemporalYearMany.class,
            TemporalYearMonth.class,
            TemporalYearMonthMany.class
        ).writeTo(tmp);
        assertThat(written).hasSize(18);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(TemporalLocalDate.class).generate().asString(), "TemporalLocalDate");
        assertStructure(TestKit.of(TemporalLocalDateMany.class).generate().asString(), "TemporalLocalDateMany");
        assertStructure(TestKit.of(TemporalLocalDateTime.class).generate().asString(), "TemporalLocalDateTime");
        assertStructure(TestKit.of(TemporalLocalDateTimeMany.class).generate().asString(), "TemporalLocalDateTimeMany");
        assertStructure(TestKit.of(TemporalLocalTime.class).generate().asString(), "TemporalLocalTime");
        assertStructure(TestKit.of(TemporalLocalTimeMany.class).generate().asString(), "TemporalLocalTimeMany");
        assertStructure(TestKit.of(TemporalInstant.class).generate().asString(), "TemporalInstant");
        assertStructure(TestKit.of(TemporalInstantMany.class).generate().asString(), "TemporalInstantMany");
        assertStructure(TestKit.of(TemporalZonedDateTime.class).generate().asString(), "TemporalZonedDateTime");
        assertStructure(TestKit.of(TemporalZonedDateTimeMany.class).generate().asString(), "TemporalZonedDateTimeMany");
        assertStructure(TestKit.of(TemporalOffsetDateTime.class).generate().asString(), "TemporalOffsetDateTime");
        assertStructure(TestKit.of(TemporalOffsetDateTimeMany.class).generate().asString(), "TemporalOffsetDateTimeMany");
        assertStructure(TestKit.of(TemporalDate.class).generate().asString(), "TemporalDate");
        assertStructure(TestKit.of(TemporalDateMany.class).generate().asString(), "TemporalDateMany");
        assertStructure(TestKit.of(TemporalYear.class).generate().asString(), "TemporalYear");
        assertStructure(TestKit.of(TemporalYearMany.class).generate().asString(), "TemporalYearMany");
        assertStructure(TestKit.of(TemporalYearMonth.class).generate().asString(), "TemporalYearMonth");
        assertStructure(TestKit.of(TemporalYearMonthMany.class).generate().asString(), "TemporalYearMonthMany");
    }

    private static void assertStructure(String src, String className) {
        // Accept either a full generated test or a placeholder comment file
        boolean isFull = src.contains("class " + className + "Test")
                && src.contains("class SmokeTest");
        boolean isPlaceholder = src.contains("TODO: " + className + "Test");
        assertThat(isFull || isPlaceholder)
                .as("Expected full test or placeholder for %s", className)
                .isTrue();
    }
}
