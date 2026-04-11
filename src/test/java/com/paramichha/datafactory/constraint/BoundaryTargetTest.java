package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.generation.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 90%+ branch coverage for {@link BoundaryTarget}.
 *
 * Covers:
 *  — all four variants: Semantic, Fixed, DecimalFixed, Special
 *  — label() dispatch (all four arms)
 *  — targetQuantity() (Fixed, DecimalFixed, and default null arms)
 *  — isSemantic() / isFixed() (true and false paths)
 *  — every factory method (integer and decimal)
 *  — decimal step calculation for justAboveDecimalMin, justBelowDecimalMax, decimalMidpoint
 */
@DisplayName("BoundaryTarget")
class BoundaryTargetTest {

    // ═══════════════════════════════════════════════════════════════════════
    // Semantic variant
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Semantic")
    class SemanticVariant {

        private final BoundaryTarget t = BoundaryTarget.semantic();

        @Test void label_is_semantic()       { assertThat(t.label()).isEqualTo("semantic"); }
        @Test void isSemantic_true()         { assertThat(t.isSemantic()).isTrue(); }
        @Test void isFixed_false()           { assertThat(t.isFixed()).isFalse(); }
        @Test void targetQuantity_null()     { assertThat(t.targetQuantity()).isNull(); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixed variant — integer factories
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Fixed — integer factories")
    class FixedVariant {

        @Test
        void atMin() {
            var t = BoundaryTarget.atMin(5L);
            assertThat(t.label()).isEqualTo("atMin");
            assertThat(t.targetQuantity()).isEqualTo(5L);
            assertThat(t.isSemantic()).isFalse();
            assertThat(t.isFixed()).isTrue();
        }

        @Test
        void justAboveMin() {
            var t = BoundaryTarget.justAboveMin(10L);
            assertThat(t.label()).isEqualTo("justAboveMin");
            assertThat(t.targetQuantity()).isEqualTo(11L);
        }

        @Test
        void midpoint() {
            var t = BoundaryTarget.midpoint(2L, 8L);
            assertThat(t.label()).isEqualTo("midpoint");
            assertThat(t.targetQuantity()).isEqualTo(5L);
        }

        @Test
        void justBelowMax() {
            var t = BoundaryTarget.justBelowMax(20L);
            assertThat(t.label()).isEqualTo("justBelowMax");
            assertThat(t.targetQuantity()).isEqualTo(19L);
        }

        @Test
        void atMax() {
            var t = BoundaryTarget.atMax(100L);
            assertThat(t.label()).isEqualTo("atMax");
            assertThat(t.targetQuantity()).isEqualTo(100L);
        }

        @Test
        void typeDefaultNegative() {
            var t = BoundaryTarget.typeDefaultNegative();
            assertThat(t.label()).isEqualTo("negative");
            assertThat(t.targetQuantity()).isEqualTo(-1L);
        }

        @Test
        void typeDefaultZero() {
            var t = BoundaryTarget.typeDefaultZero();
            assertThat(t.label()).isEqualTo("zero");
            assertThat(t.targetQuantity()).isEqualTo(0L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DecimalFixed variant — decimal factories
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DecimalFixed — decimal factories")
    class DecimalFixedVariant {

        @Test
        void atDecimalMin() {
            var t = BoundaryTarget.atDecimalMin(new BigDecimal("1.50"));
            assertThat(t.label()).isEqualTo("atMin");
            assertThat(t.targetQuantity()).isEqualTo(1L);   // longValue of 1.50
            assertThat(t.isFixed()).isTrue();
            assertThat(t.isSemantic()).isFalse();
        }

        @Test
        void atDecimalMax() {
            var t = BoundaryTarget.atDecimalMax(new BigDecimal("99.99"));
            assertThat(t.label()).isEqualTo("atMax");
            assertThat(t.targetQuantity()).isEqualTo(99L);
        }

        @Test
        void justAboveDecimalMin_adds_one_ulp() {
            // scale=2 → step = 0.01; 1.50 + 0.01 = 1.51
            var t = BoundaryTarget.justAboveDecimalMin(new BigDecimal("1.50"));
            assertThat(t.label()).isEqualTo("justAboveMin");
            var dec = (BoundaryTarget.DecimalFixed) t;
            assertThat(dec.value()).isEqualByComparingTo("1.51");
        }

        @Test
        void justBelowDecimalMax_subtracts_one_ulp() {
            // scale=2 → step = 0.01; 9.99 - 0.01 = 9.98
            var t = BoundaryTarget.justBelowDecimalMax(new BigDecimal("9.99"));
            assertThat(t.label()).isEqualTo("justBelowMax");
            var dec = (BoundaryTarget.DecimalFixed) t;
            assertThat(dec.value()).isEqualByComparingTo("9.98");
        }

        @Test
        void decimalMidpoint_average_of_min_and_max() {
            // (1.00 + 3.00) / 2 = 2.00
            var t = BoundaryTarget.decimalMidpoint(new BigDecimal("1.00"), new BigDecimal("3.00"));
            assertThat(t.label()).isEqualTo("midpoint");
            var dec = (BoundaryTarget.DecimalFixed) t;
            assertThat(dec.value()).isEqualByComparingTo("2.00");
        }

        @Test
        void decimalMidpoint_odd_rounds_half_up() {
            // (1.00 + 2.00) / 2 = 1.50
            var t = BoundaryTarget.decimalMidpoint(new BigDecimal("1.00"), new BigDecimal("2.00"));
            var dec = (BoundaryTarget.DecimalFixed) t;
            assertThat(dec.value()).isEqualByComparingTo("1.50");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Special variant
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Special")
    class SpecialVariant {

        @Test
        void nullTarget() {
            var t = BoundaryTarget.nullTarget();
            assertThat(t.label()).isEqualTo("null");
            assertThat(t.targetQuantity()).isNull();
            assertThat(t.isSemantic()).isFalse();
            assertThat(t.isFixed()).isFalse();
        }

        @Test
        void trueTarget() {
            var t = BoundaryTarget.trueTarget();
            assertThat(t.label()).isEqualTo("true");
            assertThat(t.targetQuantity()).isNull();
        }

        @Test
        void falseTarget() {
            var t = BoundaryTarget.falseTarget();
            assertThat(t.label()).isEqualTo("false");
        }

        @Test
        void allEnumValues() {
            var t = BoundaryTarget.allEnumValues();
            assertThat(t.label()).isEqualTo("allEnumValues");
            assertThat(t.targetQuantity()).isNull();
        }
    }
}
