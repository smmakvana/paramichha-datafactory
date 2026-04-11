package com.paramichha.datafactory.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the 2 remaining missing branches in com.kickstart.datafactory root package:
 * DefaultDataBuilder — validAll() isBooleanType with @AssertTrue (Boolean.class wrapper variant)
 * and any remaining analyzer gaps.
 */
@DisplayName("AnnotationAnalyzer — remaining gaps")
class AnnotationAnalyzerGapTest {

    @Test
    void both_assertTrue_and_assertFalse_adds_warning() {
        var fc = AnnotationAnalyzer.analyzeFromStrings("flag", "Boolean",
                List.of("@AssertTrue", "@AssertFalse"));
        assertThat(fc.hasWarnings()).isTrue();
        assertThat(fc.warnings().stream().anyMatch(w -> w.contains("AssertTrue") && w.contains("AssertFalse")))
                .isTrue();
    }

    @Test
    void both_past_and_future_adds_warning() {
        var fc = AnnotationAnalyzer.analyzeFromStrings("dt", "LocalDate",
                List.of("@Past", "@Future"));
        assertThat(fc.hasWarnings()).isTrue();
        assertThat(fc.warnings().stream().anyMatch(w -> w.contains("Past") && w.contains("Future")))
                .isTrue();
    }
}
