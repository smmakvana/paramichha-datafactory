package com.paramichha.datafactory.constraint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 90%+ branch coverage for {@link QuantityBounds}.
 *
 * Covers:
 *  — all three variants: Unbounded, IntegerBounds, DecimalBounds
 *  — every accessor: hasMin, hasMax, hasBoth, isContradictory, isUnbounded
 *  — interface-level min()/max() dispatch (all three switch arms each)
 *  — interface-level hasMin/hasMax/hasBoth (via all variant dispatch)
 *  — isContradictory switch (all three arms)
 *  — all factory methods
 *  — intersect: all 9 combination arms
 *  — promoteAndIntersect: all four null/non-null combinations for dMin/dMax
 *  — maxOf / minOf null-first / null-second / both-present arms
 *  — maxOfDec / minOfDec null-first / null-second / both-present arms
 */
@DisplayName("QuantityBounds")
class QuantityBoundsTest {

    // ═══════════════════════════════════════════════════════════════════════
    // Unbounded variant
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Unbounded")
    class UnboundedTests {

        private final QuantityBounds u = QuantityBounds.unbounded();

        @Test void isUnbounded_true()        { assertThat(u.isUnbounded()).isTrue(); }
        @Test void min_null()                { assertThat(u.min()).isNull(); }
        @Test void max_null()                { assertThat(u.max()).isNull(); }
        @Test void hasMin_false()            { assertThat(u.hasMin()).isFalse(); }
        @Test void hasMax_false()            { assertThat(u.hasMax()).isFalse(); }
        @Test void hasBoth_false()           { assertThat(u.hasBoth()).isFalse(); }
        @Test void isContradictory_false()   { assertThat(u.isContradictory()).isFalse(); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // IntegerBounds variant
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("IntegerBounds")
    class IntegerBoundsTests {

        @Test
        void hasMin_true_hasMax_false() {
            var b = QuantityBounds.atLeast(5);
            assertThat(b.hasMin()).isTrue();
            assertThat(b.hasMax()).isFalse();
            assertThat(b.hasBoth()).isFalse();
            assertThat(b.min()).isEqualTo(5L);
            assertThat(b.max()).isNull();
            assertThat(b.isContradictory()).isFalse();
            assertThat(b.isUnbounded()).isFalse();
        }

        @Test
        void hasMin_false_hasMax_true() {
            var b = QuantityBounds.atMost(100);
            assertThat(b.hasMin()).isFalse();
            assertThat(b.hasMax()).isTrue();
            assertThat(b.hasBoth()).isFalse();
            assertThat(b.min()).isNull();
            assertThat(b.max()).isEqualTo(100L);
        }

        @Test
        void hasBoth_true_notContradictory() {
            var b = QuantityBounds.of(1L, 10L);
            assertThat(b.hasBoth()).isTrue();
            assertThat(b.isContradictory()).isFalse();
        }

        @Test
        void isContradictory_true_when_min_greater_than_max() {
            var b = QuantityBounds.of(10L, 5L);
            assertThat(b.isContradictory()).isTrue();
        }

        @Test
        void exactly_factory_sets_both_equal() {
            var b = QuantityBounds.exactly(42);
            assertThat(b.min()).isEqualTo(42L);
            assertThat(b.max()).isEqualTo(42L);
            assertThat(b.hasBoth()).isTrue();
            assertThat(b.isContradictory()).isFalse();
        }

        @Test
        void of_both_null() {
            var b = QuantityBounds.of(null, null);
            assertThat(b.hasMin()).isFalse();
            assertThat(b.hasMax()).isFalse();
            assertThat(b.isContradictory()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DecimalBounds variant
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DecimalBounds")
    class DecimalBoundsTests {

        @Test
        void hasMin_true_hasMax_false() {
            var b = QuantityBounds.atLeastDecimal(new BigDecimal("1.5"));
            assertThat(b.hasMin()).isTrue();
            assertThat(b.hasMax()).isFalse();
            assertThat(b.hasBoth()).isFalse();
            assertThat(b.min()).isEqualTo(1L);  // longValue of 1.5
            assertThat(b.max()).isNull();
            assertThat(b.isContradictory()).isFalse();
            assertThat(b.isUnbounded()).isFalse();
        }

        @Test
        void hasMin_false_hasMax_true() {
            var b = QuantityBounds.atMostDecimal(new BigDecimal("99.9"));
            assertThat(b.hasMin()).isFalse();
            assertThat(b.hasMax()).isTrue();
            assertThat(b.hasBoth()).isFalse();
            assertThat(b.min()).isNull();
            assertThat(b.max()).isEqualTo(99L);
        }

        @Test
        void hasBoth_true_notContradictory() {
            var b = new QuantityBounds.DecimalBounds(new BigDecimal("1.0"), new BigDecimal("9.0"));
            assertThat(b.hasBoth()).isTrue();
            assertThat(b.isContradictory()).isFalse();
        }

        @Test
        void isContradictory_true_when_decMin_greater_than_decMax() {
            var b = new QuantityBounds.DecimalBounds(new BigDecimal("10.0"), new BigDecimal("5.0"));
            assertThat(b.isContradictory()).isTrue();
        }

        @Test
        void both_null_decimalBounds() {
            var b = new QuantityBounds.DecimalBounds(null, null);
            assertThat(b.hasMin()).isFalse();
            assertThat(b.hasMax()).isFalse();
            assertThat(b.min()).isNull();
            assertThat(b.max()).isNull();
            assertThat(b.isContradictory()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // intersect — all 9 combinations
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("intersect")
    class IntersectTests {

        // 1. Unbounded ∩ anything → other
        @Test
        void unbounded_intersect_unbounded() {
            var r = QuantityBounds.unbounded().intersect(QuantityBounds.unbounded());
            assertThat(r.isUnbounded()).isTrue();
        }

        @Test
        void unbounded_intersect_integer() {
            var r = QuantityBounds.unbounded().intersect(QuantityBounds.of(2L, 8L));
            assertThat(r.min()).isEqualTo(2L);
            assertThat(r.max()).isEqualTo(8L);
        }

        @Test
        void unbounded_intersect_decimal() {
            var r = QuantityBounds.unbounded().intersect(
                    QuantityBounds.atLeastDecimal(new BigDecimal("1.5")));
            assertThat(r.hasMin()).isTrue();
        }

        // 2. IntegerBounds ∩ Unbounded → self
        @Test
        void integer_intersect_unbounded() {
            var r = QuantityBounds.of(3L, 7L).intersect(QuantityBounds.unbounded());
            assertThat(r.min()).isEqualTo(3L);
            assertThat(r.max()).isEqualTo(7L);
        }

        // 3. IntegerBounds ∩ IntegerBounds → narrower range
        @Test
        void integer_intersect_integer_overlapping() {
            var r = QuantityBounds.of(2L, 10L).intersect(QuantityBounds.of(5L, 15L));
            assertThat(r.min()).isEqualTo(5L);
            assertThat(r.max()).isEqualTo(10L);
        }

        @Test
        void integer_intersect_integer_one_null_min() {
            // maxOf: a=null → returns b
            var r = QuantityBounds.atMost(10L).intersect(QuantityBounds.atLeast(3L));
            assertThat(r.min()).isEqualTo(3L);
            assertThat(r.max()).isEqualTo(10L);
        }

        @Test
        void integer_intersect_integer_one_null_max() {
            // minOf: a=null → returns b
            var r = QuantityBounds.atLeast(5L).intersect(QuantityBounds.atMost(20L));
            assertThat(r.min()).isEqualTo(5L);
            assertThat(r.max()).isEqualTo(20L);
        }

        @Test
        void integer_intersect_integer_both_max_null() {
            // minOf: b=null → returns a (which is also null)
            var r = QuantityBounds.atLeast(1L).intersect(QuantityBounds.atLeast(2L));
            assertThat(r.min()).isEqualTo(2L);
            assertThat(r.max()).isNull();
        }

        // 4. IntegerBounds ∩ DecimalBounds → promote
        @Test
        void integer_intersect_decimal() {
            var r = QuantityBounds.of(1L, 100L).intersect(
                    QuantityBounds.atLeastDecimal(new BigDecimal("10.5")));
            assertThat(r).isInstanceOf(QuantityBounds.DecimalBounds.class);
            assertThat(r.hasMin()).isTrue();
        }

        // 5. DecimalBounds ∩ Unbounded → self
        @Test
        void decimal_intersect_unbounded() {
            var dec = QuantityBounds.atLeastDecimal(new BigDecimal("3.0"));
            var r = dec.intersect(QuantityBounds.unbounded());
            assertThat(r.hasMin()).isTrue();
        }

        // 6. DecimalBounds ∩ IntegerBounds → promote
        @Test
        void decimal_intersect_integer() {
            var r = QuantityBounds.atLeastDecimal(new BigDecimal("2.5"))
                    .intersect(QuantityBounds.atMost(50L));
            assertThat(r).isInstanceOf(QuantityBounds.DecimalBounds.class);
            assertThat(r.hasMax()).isTrue();
        }

        // 7. DecimalBounds ∩ DecimalBounds
        @Test
        void decimal_intersect_decimal() {
            var r = new QuantityBounds.DecimalBounds(new BigDecimal("1.0"), new BigDecimal("10.0"))
                    .intersect(new QuantityBounds.DecimalBounds(new BigDecimal("3.0"), new BigDecimal("7.0")));
            assertThat(r.min()).isEqualTo(3L);
            assertThat(r.max()).isEqualTo(7L);
        }

        @Test
        void decimal_intersect_decimal_nulls() {
            // maxOfDec: a=null → returns b; minOfDec: b=null → returns a
            var r = new QuantityBounds.DecimalBounds(null, new BigDecimal("20.0"))
                    .intersect(new QuantityBounds.DecimalBounds(new BigDecimal("5.0"), null));
            assertThat(r.hasMin()).isTrue();
            assertThat(r.hasMax()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // promoteAndIntersect — all four dMin/dMax null-combination paths
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("promoteAndIntersect — null/non-null combinations")
    class PromoteAndIntersect {

        // IntegerBounds has both min+max, DecimalBounds has both min+max
        // → both if-branches fire (dMin = max(i.min, d.decMin), dMax = min(i.max, d.decMax))
        @Test
        void both_integer_and_decimal_bounds_present() {
            var r = QuantityBounds.of(5L, 50L)
                    .intersect(new QuantityBounds.DecimalBounds(new BigDecimal("10.0"), new BigDecimal("40.0")));
            var dec = (QuantityBounds.DecimalBounds) r;
            assertThat(dec.decMin()).isEqualByComparingTo("10.0");
            assertThat(dec.decMax()).isEqualByComparingTo("40.0");
        }

        // IntegerBounds min only (no max), DecimalBounds max only (no min)
        // → dMin = i.min promoted, dMax = d.decMax; neither if-branch fires
        @Test
        void integer_min_only_decimal_max_only() {
            var r = QuantityBounds.atLeast(3L)
                    .intersect(QuantityBounds.atMostDecimal(new BigDecimal("30.0")));
            var dec = (QuantityBounds.DecimalBounds) r;
            assertThat(dec.decMin()).isEqualByComparingTo("3");
            assertThat(dec.decMax()).isEqualByComparingTo("30.0");
        }

        // IntegerBounds max only (no min), DecimalBounds min only (no max)
        @Test
        void integer_max_only_decimal_min_only() {
            var r = QuantityBounds.atMost(20L)
                    .intersect(QuantityBounds.atLeastDecimal(new BigDecimal("5.5")));
            var dec = (QuantityBounds.DecimalBounds) r;
            assertThat(dec.decMin()).isEqualByComparingTo("5.5");
            assertThat(dec.decMax()).isEqualByComparingTo("20");
        }

        // Both have null min, both have null max
        @Test
        void both_unbounded_decimal() {
            var r = QuantityBounds.of(null, null)
                    .intersect(new QuantityBounds.DecimalBounds(null, null));
            var dec = (QuantityBounds.DecimalBounds) r;
            assertThat(dec.decMin()).isNull();
            assertThat(dec.decMax()).isNull();
        }
    }
}
