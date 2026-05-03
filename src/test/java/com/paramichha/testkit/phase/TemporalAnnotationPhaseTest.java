package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastLocalDate;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentLocalDate;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureLocalDate;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentLocalDate;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastLocalDateTime;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentLocalDateTime;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureLocalDateTime;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentLocalDateTime;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastInstant;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentInstant;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureInstant;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentInstant;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastZonedDateTime;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentZonedDateTime;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureZonedDateTime;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentZonedDateTime;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastOffsetDateTime;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentOffsetDateTime;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureOffsetDateTime;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentOffsetDateTime;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastDate;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentDate;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureDate;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentDate;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastYear;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentYear;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureYear;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentYear;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastYearMonth;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentYearMonth;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureYearMonth;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentYearMonth;
import com.paramichha.testkit.fixture.annotation.past.AnnotationPastMany;
import com.paramichha.testkit.fixture.annotation.pastorpresent.AnnotationPastOrPresentMany;
import com.paramichha.testkit.fixture.annotation.future.AnnotationFutureMany;
import com.paramichha.testkit.fixture.annotation.futureorpresent.AnnotationFutureOrPresentMany;
import com.paramichha.testkit.fixture.annotation.asserttrue.AnnotationAssertTrueBoolean;
import com.paramichha.testkit.fixture.annotation.asserttrue.AnnotationAssertTruePrimitive;
import com.paramichha.testkit.fixture.annotation.asserttrue.AnnotationAssertTrueMany;
import com.paramichha.testkit.fixture.annotation.assertfalse.AnnotationAssertFalseBoolean;
import com.paramichha.testkit.fixture.annotation.assertfalse.AnnotationAssertFalsePrimitive;
import com.paramichha.testkit.fixture.annotation.assertfalse.AnnotationAssertFalseMany;
import com.paramichha.testkit.fixture.annotation.valid.AnnotationValidNested;

@DisplayName("Phase 6 — Temporal and boolean annotations")
class TemporalAnnotationPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            AnnotationPastLocalDate.class,
            AnnotationPastOrPresentLocalDate.class,
            AnnotationFutureLocalDate.class,
            AnnotationFutureOrPresentLocalDate.class,
            AnnotationPastLocalDateTime.class,
            AnnotationPastOrPresentLocalDateTime.class,
            AnnotationFutureLocalDateTime.class,
            AnnotationFutureOrPresentLocalDateTime.class,
            AnnotationPastInstant.class,
            AnnotationPastOrPresentInstant.class,
            AnnotationFutureInstant.class,
            AnnotationFutureOrPresentInstant.class,
            AnnotationPastZonedDateTime.class,
            AnnotationPastOrPresentZonedDateTime.class,
            AnnotationFutureZonedDateTime.class,
            AnnotationFutureOrPresentZonedDateTime.class,
            AnnotationPastOffsetDateTime.class,
            AnnotationPastOrPresentOffsetDateTime.class,
            AnnotationFutureOffsetDateTime.class,
            AnnotationFutureOrPresentOffsetDateTime.class,
            AnnotationPastDate.class,
            AnnotationPastOrPresentDate.class,
            AnnotationFutureDate.class,
            AnnotationFutureOrPresentDate.class,
            AnnotationPastYear.class,
            AnnotationPastOrPresentYear.class,
            AnnotationFutureYear.class,
            AnnotationFutureOrPresentYear.class,
            AnnotationPastYearMonth.class,
            AnnotationPastOrPresentYearMonth.class,
            AnnotationFutureYearMonth.class,
            AnnotationFutureOrPresentYearMonth.class,
            AnnotationPastMany.class,
            AnnotationPastOrPresentMany.class,
            AnnotationFutureMany.class,
            AnnotationFutureOrPresentMany.class,
            AnnotationAssertTrueBoolean.class,
            AnnotationAssertTruePrimitive.class,
            AnnotationAssertTrueMany.class,
            AnnotationAssertFalseBoolean.class,
            AnnotationAssertFalsePrimitive.class,
            AnnotationAssertFalseMany.class,
            AnnotationValidNested.class
        ).writeTo(tmp);
        assertThat(written).hasSize(43);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(AnnotationPastLocalDate.class).generate().asString(), "AnnotationPastLocalDate");
        assertStructure(TestKit.of(AnnotationPastOrPresentLocalDate.class).generate().asString(), "AnnotationPastOrPresentLocalDate");
        assertStructure(TestKit.of(AnnotationFutureLocalDate.class).generate().asString(), "AnnotationFutureLocalDate");
        assertStructure(TestKit.of(AnnotationFutureOrPresentLocalDate.class).generate().asString(), "AnnotationFutureOrPresentLocalDate");
        assertStructure(TestKit.of(AnnotationPastLocalDateTime.class).generate().asString(), "AnnotationPastLocalDateTime");
        assertStructure(TestKit.of(AnnotationPastOrPresentLocalDateTime.class).generate().asString(), "AnnotationPastOrPresentLocalDateTime");
        assertStructure(TestKit.of(AnnotationFutureLocalDateTime.class).generate().asString(), "AnnotationFutureLocalDateTime");
        assertStructure(TestKit.of(AnnotationFutureOrPresentLocalDateTime.class).generate().asString(), "AnnotationFutureOrPresentLocalDateTime");
        assertStructure(TestKit.of(AnnotationPastInstant.class).generate().asString(), "AnnotationPastInstant");
        assertStructure(TestKit.of(AnnotationPastOrPresentInstant.class).generate().asString(), "AnnotationPastOrPresentInstant");
        assertStructure(TestKit.of(AnnotationFutureInstant.class).generate().asString(), "AnnotationFutureInstant");
        assertStructure(TestKit.of(AnnotationFutureOrPresentInstant.class).generate().asString(), "AnnotationFutureOrPresentInstant");
        assertStructure(TestKit.of(AnnotationPastZonedDateTime.class).generate().asString(), "AnnotationPastZonedDateTime");
        assertStructure(TestKit.of(AnnotationPastOrPresentZonedDateTime.class).generate().asString(), "AnnotationPastOrPresentZonedDateTime");
        assertStructure(TestKit.of(AnnotationFutureZonedDateTime.class).generate().asString(), "AnnotationFutureZonedDateTime");
        assertStructure(TestKit.of(AnnotationFutureOrPresentZonedDateTime.class).generate().asString(), "AnnotationFutureOrPresentZonedDateTime");
        assertStructure(TestKit.of(AnnotationPastOffsetDateTime.class).generate().asString(), "AnnotationPastOffsetDateTime");
        assertStructure(TestKit.of(AnnotationPastOrPresentOffsetDateTime.class).generate().asString(), "AnnotationPastOrPresentOffsetDateTime");
        assertStructure(TestKit.of(AnnotationFutureOffsetDateTime.class).generate().asString(), "AnnotationFutureOffsetDateTime");
        assertStructure(TestKit.of(AnnotationFutureOrPresentOffsetDateTime.class).generate().asString(), "AnnotationFutureOrPresentOffsetDateTime");
        assertStructure(TestKit.of(AnnotationPastDate.class).generate().asString(), "AnnotationPastDate");
        assertStructure(TestKit.of(AnnotationPastOrPresentDate.class).generate().asString(), "AnnotationPastOrPresentDate");
        assertStructure(TestKit.of(AnnotationFutureDate.class).generate().asString(), "AnnotationFutureDate");
        assertStructure(TestKit.of(AnnotationFutureOrPresentDate.class).generate().asString(), "AnnotationFutureOrPresentDate");
        assertStructure(TestKit.of(AnnotationPastYear.class).generate().asString(), "AnnotationPastYear");
        assertStructure(TestKit.of(AnnotationPastOrPresentYear.class).generate().asString(), "AnnotationPastOrPresentYear");
        assertStructure(TestKit.of(AnnotationFutureYear.class).generate().asString(), "AnnotationFutureYear");
        assertStructure(TestKit.of(AnnotationFutureOrPresentYear.class).generate().asString(), "AnnotationFutureOrPresentYear");
        assertStructure(TestKit.of(AnnotationPastYearMonth.class).generate().asString(), "AnnotationPastYearMonth");
        assertStructure(TestKit.of(AnnotationPastOrPresentYearMonth.class).generate().asString(), "AnnotationPastOrPresentYearMonth");
        assertStructure(TestKit.of(AnnotationFutureYearMonth.class).generate().asString(), "AnnotationFutureYearMonth");
        assertStructure(TestKit.of(AnnotationFutureOrPresentYearMonth.class).generate().asString(), "AnnotationFutureOrPresentYearMonth");
        assertStructure(TestKit.of(AnnotationPastMany.class).generate().asString(), "AnnotationPastMany");
        assertStructure(TestKit.of(AnnotationPastOrPresentMany.class).generate().asString(), "AnnotationPastOrPresentMany");
        assertStructure(TestKit.of(AnnotationFutureMany.class).generate().asString(), "AnnotationFutureMany");
        assertStructure(TestKit.of(AnnotationFutureOrPresentMany.class).generate().asString(), "AnnotationFutureOrPresentMany");
        assertStructure(TestKit.of(AnnotationAssertTrueBoolean.class).generate().asString(), "AnnotationAssertTrueBoolean");
        assertStructure(TestKit.of(AnnotationAssertTruePrimitive.class).generate().asString(), "AnnotationAssertTruePrimitive");
        assertStructure(TestKit.of(AnnotationAssertTrueMany.class).generate().asString(), "AnnotationAssertTrueMany");
        assertStructure(TestKit.of(AnnotationAssertFalseBoolean.class).generate().asString(), "AnnotationAssertFalseBoolean");
        assertStructure(TestKit.of(AnnotationAssertFalsePrimitive.class).generate().asString(), "AnnotationAssertFalsePrimitive");
        assertStructure(TestKit.of(AnnotationAssertFalseMany.class).generate().asString(), "AnnotationAssertFalseMany");
        assertStructure(TestKit.of(AnnotationValidNested.class).generate().asString(), "AnnotationValidNested");
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
