package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers all remaining missing branches in:
 * TemporalShaper  — NONE direction for Instant/LocalDate/LocalDateTime/ZonedDateTime/OffsetDateTime
 * NumericShaper   — bounded() clamp arms, shapeDouble() all arms
 * StringShaper    — fitEmail no-@, exact-length, needed<=0; fitUrl exact-length
 */
@DisplayName("Shaper — remaining branch coverage")
class ShaperBranchTest {

    // ═══════════════════════════════════════════════════════════════════════
    // TemporalShaper — NONE direction for types not yet covered
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TemporalShaper — NONE direction (no annotation)")
    class TemporalNone {

        private Object shape(String type) {
            var fc = AnnotationAnalyzer.analyzeFromStrings("f", type, List.of());
            return TemporalShaper.shape(fc, BoundaryTarget.semantic());
        }

        @Test
        void instant_none() {
            assertThat(shape("Instant")).isInstanceOf(Instant.class);
        }

        @Test
        void localDate_none() {
            assertThat(shape("LocalDate")).isInstanceOf(LocalDate.class);
        }

        @Test
        void localDateTime_none() {
            assertThat(shape("LocalDateTime")).isInstanceOf(LocalDateTime.class);
        }

        @Test
        void zonedDateTime_none() {
            assertThat(shape("ZonedDateTime")).isInstanceOf(ZonedDateTime.class);
        }

        @Test
        void offsetDateTime_none() {
            assertThat(shape("OffsetDateTime")).isInstanceOf(OffsetDateTime.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NumericShaper — bounded() clamp branches and shapeDouble() arms
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("NumericShaper — bounded() and shapeDouble() branches")
    class NumericBranches {

        private Object shape(String name, String type, String... anns) {
            var fc = AnnotationAnalyzer.analyzeFromStrings(name, type, List.of(anns));
            return NumericShaper.shape(fc, BoundaryTarget.semantic());
        }

        // bounded() — preferred < min → result clamped up to min
        @Test
        void bounded_clamped_to_min() {
            // "age" hint prefers 30, but min=50 → result must be >= 50
            var fc = AnnotationAnalyzer.analyzeFromStrings("age", "Integer", List.of("@Min(50)", "@Max(200)"));
            var v = (Integer) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isGreaterThanOrEqualTo(50);
        }

        // bounded() — preferred > max → result clamped down to max
        @Test
        void bounded_clamped_to_max() {
            // "price" hint prefers 100, but max=5 → result must be <= 5
            var fc = AnnotationAnalyzer.analyzeFromStrings("price", "Integer", List.of("@Min(1)", "@Max(5)"));
            var v = (Integer) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isLessThanOrEqualTo(5);
        }

        // shapeDouble — both min and max → midpoint
        @Test
        void shapeDouble_both_bounds_midpoint() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Double", List.of("@Min(10)", "@Max(20)"));
            var v = (Double) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isEqualTo(15.0d);
        }

        // shapeDouble — min only → min + 0.5
        @Test
        void shapeDouble_min_only() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Double", List.of("@Min(10)"));
            var v = (Double) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isEqualTo(10.5d);
        }

        // shapeDouble — max only → max - 0.5
        @Test
        void shapeDouble_max_only() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Double", List.of("@Max(20)"));
            var v = (Double) NumericShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isEqualTo(19.5d);
        }

        // semanticValue — quantity/qty hint
        @Test
        void quantity_hint() {
            assertThat((Integer) shape("quantity", "Integer")).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // StringShaper — fitEmail and fitUrl exact and edge paths
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("StringShaper — fitEmail and fitUrl edge cases")
    class StringEdges {

        // fitEmail — no '@' in input → falls back to fitPlain
        @Test
        void fitEmail_no_at_sign() {
            String result = StringShaper.fitEmail("noemail", 10);
            assertThat(result).hasSize(10);
        }

        // fitEmail — email.length() == target → returned as-is
        @Test
        void fitEmail_exact_length() {
            String email = "a@b.com"; // 7 chars
            assertThat(StringShaper.fitEmail(email, 7)).isEqualTo(email);
        }

        // fitEmail — needed <= 0 after local fills gap → return local + domain without domain extension
        @Test
        void fitEmail_local_fills_all_needed() {
            // local = "ab" (2 chars), domain = "@x.com" (6 chars), total = 8
            // target = 10, needed = 2, localRoom = 62, localGrowth = 2, needed becomes 0
            String email = "ab@x.com";
            String result = StringShaper.fitEmail(email, 10);
            assertThat(result).hasSize(10);
            assertThat(result).endsWith("@x.com");
        }

        // fitUrl — url.length() == target → returned as-is
        @Test
        void fitUrl_exact_length() {
            String url = "http://x.com"; // 12 chars
            assertThat(StringShaper.fitUrl(url, 12)).isEqualTo(url);
        }

        // fitUrl — url longer than target → trimmed
        @Test
        void fitUrl_trimmed() {
            String url = "http://example.com/path";
            String result = StringShaper.fitUrl(url, 10);
            assertThat(result).hasSize(10);
        }

        // fitUrl — url shorter than target → padded
        @Test
        void fitUrl_padded() {
            String url = "http://x.io";
            String result = StringShaper.fitUrl(url, 20);
            assertThat(result).hasSize(20);
        }
    }
}
