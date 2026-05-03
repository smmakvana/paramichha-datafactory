package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullEmail;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullEmailSize;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotBlankSize;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotBlankEmailSize;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullMinMax;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullPositive;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullNegative;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullPast;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullFuture;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullPastOrPresent;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullFutureOrPresent;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationPositiveOrZeroMax;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNegativeOrZeroMin;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationMinMaxMany;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationDecimalMinMaxBigDecimal;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullString;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullNumeric;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullTemporal;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullMixed;

@DisplayName("Phase 7 — Combined annotations")
class CombinedAnnotationPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            AnnotationNotNullEmail.class,
            AnnotationNotNullEmailSize.class,
            AnnotationNotBlankSize.class,
            AnnotationNotBlankEmailSize.class,
            AnnotationNotNullMinMax.class,
            AnnotationNotNullPositive.class,
            AnnotationNotNullNegative.class,
            AnnotationNotNullPast.class,
            AnnotationNotNullFuture.class,
            AnnotationNotNullPastOrPresent.class,
            AnnotationNotNullFutureOrPresent.class,
            AnnotationPositiveOrZeroMax.class,
            AnnotationNegativeOrZeroMin.class,
            AnnotationMinMaxMany.class,
            AnnotationDecimalMinMaxBigDecimal.class,
            AnnotationFullString.class,
            AnnotationFullNumeric.class,
            AnnotationFullTemporal.class,
            AnnotationFullMixed.class
        ).writeTo(tmp);
        assertThat(written).hasSize(19);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(AnnotationNotNullEmail.class).generate().asString(), "AnnotationNotNullEmail");
        assertStructure(TestKit.of(AnnotationNotNullEmailSize.class).generate().asString(), "AnnotationNotNullEmailSize");
        assertStructure(TestKit.of(AnnotationNotBlankSize.class).generate().asString(), "AnnotationNotBlankSize");
        assertStructure(TestKit.of(AnnotationNotBlankEmailSize.class).generate().asString(), "AnnotationNotBlankEmailSize");
        assertStructure(TestKit.of(AnnotationNotNullMinMax.class).generate().asString(), "AnnotationNotNullMinMax");
        assertStructure(TestKit.of(AnnotationNotNullPositive.class).generate().asString(), "AnnotationNotNullPositive");
        assertStructure(TestKit.of(AnnotationNotNullNegative.class).generate().asString(), "AnnotationNotNullNegative");
        assertStructure(TestKit.of(AnnotationNotNullPast.class).generate().asString(), "AnnotationNotNullPast");
        assertStructure(TestKit.of(AnnotationNotNullFuture.class).generate().asString(), "AnnotationNotNullFuture");
        assertStructure(TestKit.of(AnnotationNotNullPastOrPresent.class).generate().asString(), "AnnotationNotNullPastOrPresent");
        assertStructure(TestKit.of(AnnotationNotNullFutureOrPresent.class).generate().asString(), "AnnotationNotNullFutureOrPresent");
        assertStructure(TestKit.of(AnnotationPositiveOrZeroMax.class).generate().asString(), "AnnotationPositiveOrZeroMax");
        assertStructure(TestKit.of(AnnotationNegativeOrZeroMin.class).generate().asString(), "AnnotationNegativeOrZeroMin");
        assertStructure(TestKit.of(AnnotationMinMaxMany.class).generate().asString(), "AnnotationMinMaxMany");
        assertStructure(TestKit.of(AnnotationDecimalMinMaxBigDecimal.class).generate().asString(), "AnnotationDecimalMinMaxBigDecimal");
        assertStructure(TestKit.of(AnnotationFullString.class).generate().asString(), "AnnotationFullString");
        assertStructure(TestKit.of(AnnotationFullNumeric.class).generate().asString(), "AnnotationFullNumeric");
        assertStructure(TestKit.of(AnnotationFullTemporal.class).generate().asString(), "AnnotationFullTemporal");
        assertStructure(TestKit.of(AnnotationFullMixed.class).generate().asString(), "AnnotationFullMixed");
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
