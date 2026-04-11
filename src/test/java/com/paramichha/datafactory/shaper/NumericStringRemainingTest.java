package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers remaining branches in NumericShaper and StringShaper.
 */
@DisplayName("NumericShaper and StringShaper — remaining branches")
class NumericStringRemainingTest {

    // ═══════════════════════════════════════════════════════════════════════
    // NumericShaper — remaining 3 branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("NumericShaper — remaining semantic branches")
    class NumericRemaining {

        // semanticValue: min != null && max != null -> midpoint (line 53)
        // This path is hit when NO field-name hint matches AND both bounds present
        @Test
        void both_bounds_midpoint_no_hint() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("zzz", "Integer",
                    List.of("@Min(10)", "@Max(20)"));
            var v = (Integer) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isEqualTo(15); // (10+20)/2
        }

        // semanticValue: min only (line 54) — no hint, min present, no max
        @Test
        void min_only_no_hint() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("zzz", "Integer", List.of("@Min(7)"));
            var v = (Integer) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isEqualTo(8); // min + 1
        }

        // semanticValue: max only (line 55) — no hint, max present, no min
        @Test
        void max_only_no_hint() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("zzz", "Integer", List.of("@Max(50)"));
            var v = (Integer) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isEqualTo(49); // max - 1
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // StringShaper — remaining 9 branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("StringShaper — fitPlain and fitToBounds remaining")
    class StringRemaining {

        // fitPlain: value.length() == target -> return value as-is (line 89)
        @Test
        void fitPlain_exact_length() {
            String v = StringShaper.fitPlain("hello", 5);
            assertThat(v).isEqualTo("hello");
        }

        // fitEmail: email.length() > target AND local.length() > newLocalLen (trim local)
        @Test
        void fitEmail_trim_local() {
            // local = "abcdefgh" (8), domain = "@x.com" (6), total = 14, target = 8
            // newLocalLen = max(1, min(64, 8-6)) = 2 -> local trimmed to "ab"
            String result = StringShaper.fitEmail("abcdefgh@x.com", 8);
            assertThat(result).hasSize(8);
            assertThat(result).contains("@x.com");
        }

        // fitToBounds: default (NONE format) in max-exceeded switch
        @Test
        void fitToBounds_default_max_trim() {
            // Plain string with max bound exceeded — hits default arm in max switch
            var fc = AnnotationAnalyzer.analyzeFromStrings("desc", "String",
                    List.of("@Size(min=2, max=5)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isLessThanOrEqualTo(5);
        }

        // fitToBounds: default (NONE format) in min-not-met switch
        @Test
        void fitToBounds_default_min_pad() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("code", "String",
                    List.of("@Size(min=20, max=50)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isGreaterThanOrEqualTo(20);
        }

        // fitToBounds URL max trim — hits URL arm in max-exceeded switch
        @Test
        void fitToBounds_url_max_trim() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("site", "String",
                    List.of("@URL", "@Size(min=5, max=15)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isLessThanOrEqualTo(15);
        }

        // fitToBounds URL min pad — hits URL arm in min-not-met switch
        @Test
        void fitToBounds_url_min_pad() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("link", "String",
                    List.of("@URL", "@Size(min=40, max=200)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isGreaterThanOrEqualTo(40);
        }
    }
}
