package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers remaining gaps in NumericShaper — semantic hint branches
 * not exercised by the fixture tests.
 */
@DisplayName("NumericShaper — semantic hint gaps")
class NumericShaperGapTest {

    private static Object shape(String name, String type) {
        var fc = AnnotationAnalyzer.analyzeFromStrings(name, type, List.of());
        return NumericShaper.shape(fc, BoundaryTarget.semantic());
    }

    @Nested
    @DisplayName("semantic value field-name hints")
    class SemanticHints {

        @Test
        void retry_hint() {
            assertThat(shape("retryCount", "Integer")).isNotNull();
        }

        @Test
        void attempt_hint() {
            assertThat(shape("attempts", "Integer")).isNotNull();
        }

        @Test
        void total_hint() {
            assertThat(shape("totalCount", "Integer")).isNotNull();
        }

        @Test
        void qty_hint() {
            assertThat(shape("qty", "Integer")).isEqualTo(1);
        }

        @Test
        void page_hint() {
            assertThat(shape("pageNum", "Integer")).isNotNull();
        }

        @Test
        void score_hint() {
            assertThat(shape("score", "Integer")).isEqualTo(5);
        }

        @Test
        void price_hint() {
            assertThat(shape("price", "Integer")).isEqualTo(100);
        }

        @Test
        void amount_hint() {
            assertThat(shape("amount", "Integer")).isEqualTo(100);
        }

        @Test
        void year_hint() {
            assertThat(shape("year", "Integer")).isEqualTo(2024);
        }

        @Test
        void double_no_bounds_faker() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("value", "Double", List.of());
            assertThat(NumericShaper.shape(fc, BoundaryTarget.semantic())).isInstanceOf(Double.class);
        }

        @Test
        void double_target_quantity() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Double", List.of("@Min(1)", "@Max(10)"));
            var v = (double) NumericShaper.shape(fc, BoundaryTarget.atMin(1));
            assertThat(v).isEqualTo(1.0d);
        }

        @Test
        void bigdecimal_with_target() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("price", "BigDecimal",
                    List.of("@Digits(integer=5, fraction=2)"));
            var v = (java.math.BigDecimal) NumericShaper.shape(fc, BoundaryTarget.atMin(5));
            assertThat(v.scale()).isEqualTo(2);
        }

        @Test
        void min_only_returns_min_plus_one() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Integer", List.of("@Min(10)"));
            assertThat((Integer) NumericShaper.shape(fc, BoundaryTarget.semantic())).isEqualTo(11);
        }

        @Test
        void max_only_returns_max_minus_one() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("n", "Integer", List.of("@Max(100)"));
            assertThat((Integer) NumericShaper.shape(fc, BoundaryTarget.semantic())).isEqualTo(99);
        }
    }
}
