package com.paramichha.datafactory.planner;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.analyzer.QuantityBounds;
import com.paramichha.datafactory.analyzer.TemporalDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BoundaryPlanner")
class BoundaryPlannerTest {

    private FieldConstraints strField(String name, QuantityBounds bounds) {
        return FieldConstraints.builder(name, String.class).bounds(bounds).build();
    }

    private FieldConstraints intField(String name, QuantityBounds bounds) {
        return FieldConstraints.builder(name, Integer.class).bounds(bounds).integerOnly(true).build();
    }

    private FieldConstraints longField(String name, QuantityBounds bounds) {
        return FieldConstraints.builder(name, Long.class).bounds(bounds).integerOnly(true).build();
    }

    private List<String> labels(List<BoundaryTarget> targets) {
        return targets.stream().map(BoundaryTarget::label).toList();
    }

    private Map<String, BoundaryTarget> byLabel(List<BoundaryTarget> targets) {
        return targets.stream().collect(Collectors.toMap(BoundaryTarget::label, t -> t));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NO BOUNDS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("No bounds")
    class NoBounds {
        @Test
        void string_semanticOnly() {
            assertThat(labels(BoundaryPlanner.plan(strField("name", QuantityBounds.unbounded()))))
                    .containsExactly("semantic");
        }

        @Test
        void integer_semanticOnly() {
            assertThat(labels(BoundaryPlanner.plan(intField("count", QuantityBounds.unbounded()))))
                    .containsExactly("semantic");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MIN ONLY
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Min only")
    class MinOnly {
        @Test
        void integer_threeTargets() {
            assertThat(labels(BoundaryPlanner.plan(intField("age", QuantityBounds.atLeast(18)))))
                    .containsExactly("semantic", "atMin", "justAboveMin");
        }

        @Test
        void integer_correctQuantities() {
            var m = byLabel(BoundaryPlanner.plan(intField("age", QuantityBounds.atLeast(18))));
            assertThat(m.get("atMin").targetQuantity()).isEqualTo(18L);
            assertThat(m.get("justAboveMin").targetQuantity()).isEqualTo(19L);
            assertThat(m.get("semantic").targetQuantity()).isNull();
        }

        @Test
        void string_correctLengthTargets() {
            var m = byLabel(BoundaryPlanner.plan(strField("name", QuantityBounds.atLeast(5))));
            assertThat(m.get("atMin").targetQuantity()).isEqualTo(5L);
            assertThat(m.get("justAboveMin").targetQuantity()).isEqualTo(6L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MAX ONLY
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Max only")
    class MaxOnly {
        @Test
        void integer_threeTargets() {
            assertThat(labels(BoundaryPlanner.plan(intField("rating", QuantityBounds.atMost(5)))))
                    .containsExactly("semantic", "justBelowMax", "atMax");
        }

        @Test
        void integer_correctQuantities() {
            var m = byLabel(BoundaryPlanner.plan(intField("rating", QuantityBounds.atMost(5))));
            assertThat(m.get("atMax").targetQuantity()).isEqualTo(5L);
            assertThat(m.get("justBelowMax").targetQuantity()).isEqualTo(4L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BOTH BOUNDS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Both bounds")
    class BothBounds {
        @Test
        void sixTargets() {
            assertThat(labels(BoundaryPlanner.plan(intField("age", QuantityBounds.of(18L, 120L)))))
                    .containsExactly("semantic", "atMin", "justAboveMin", "midpoint", "justBelowMax", "atMax");
        }

        @Test
        void correctQuantities() {
            var m = byLabel(BoundaryPlanner.plan(intField("age", QuantityBounds.of(18L, 120L))));
            assertThat(m.get("atMin").targetQuantity()).isEqualTo(18L);
            assertThat(m.get("justAboveMin").targetQuantity()).isEqualTo(19L);
            assertThat(m.get("midpoint").targetQuantity()).isEqualTo(69L);
            assertThat(m.get("justBelowMax").targetQuantity()).isEqualTo(119L);
            assertThat(m.get("atMax").targetQuantity()).isEqualTo(120L);
        }

        @Test
        void string_midpointIsLengthMidpoint() {
            var m = byLabel(BoundaryPlanner.plan(strField("name", QuantityBounds.of(2L, 50L))));
            assertThat(m.get("midpoint").targetQuantity()).isEqualTo(26L);
        }

        @Test
        void allTargetsWithinBounds() {
            BoundaryPlanner.plan(intField("v", QuantityBounds.of(10L, 100L))).stream()
                    .filter(t -> t.targetQuantity() != null)
                    .forEach(t -> assertThat(t.targetQuantity()).isBetween(10L, 100L));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MIN == MAX
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Min equals Max")
    class Exact {
        @Test
        void singleTarget() {
            var targets = BoundaryPlanner.plan(intField("pin", QuantityBounds.exactly(6)));
            assertThat(labels(targets)).containsExactly("atMin");
            assertThat(targets.get(0).targetQuantity()).isEqualTo(6L);
        }

        @Test
        void string_singleTarget() {
            var targets = BoundaryPlanner.plan(strField("otp", QuantityBounds.exactly(6)));
            assertThat(labels(targets)).containsExactly("atMin");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TEMPORAL / BOOLEAN / ENUM / NULL
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Special types")
    class SpecialTypes {

        @Test
        void temporal_semanticOnly() {
            var f = FieldConstraints.builder("dob", LocalDate.class)
                    .temporal(TemporalDirection.PAST).build();
            assertThat(labels(BoundaryPlanner.plan(f))).containsExactly("semantic");
        }

        @Test
        void boolean_noAnnotation_bothValues() {
            var f = FieldConstraints.builder("active", Boolean.class).build();
            assertThat(labels(BoundaryPlanner.plan(f))).containsExactlyInAnyOrder("true", "false");
        }

        @Test
        void boolean_assertTrue_onlyTrue() {
            var f = FieldConstraints.builder("active", Boolean.class).assertTrueRequired(true).build();
            assertThat(labels(BoundaryPlanner.plan(f))).containsExactly("true");
        }

        @Test
        void boolean_assertFalse_onlyFalse() {
            var f = FieldConstraints.builder("deleted", Boolean.class).assertFalseRequired(true).build();
            assertThat(labels(BoundaryPlanner.plan(f))).containsExactly("false");
        }

        @Test
        void enum_allEnumValues() {
            var f = FieldConstraints.builder("status", Status.class).build();
            assertThat(labels(BoundaryPlanner.plan(f))).containsExactly("allEnumValues");
        }

        @Test
        void mustBeNull_nullTarget() {
            var f = FieldConstraints.builder("deprecated", String.class).mustBeNull(true).build();
            assertThat(labels(BoundaryPlanner.plan(f))).containsExactly("null");
        }

        enum Status {ACTIVE, INACTIVE}
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DEDUPLICATION
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Adjacent bounds — deduplication")
    class Deduplication {
        @Test
        void adjacentBounds_noDuplicateLabels() {
            // min=5, max=6: justAboveMin(6)==atMax(6) → should deduplicate
            var targets = BoundaryPlanner.plan(intField("flag", QuantityBounds.of(5L, 6L)));
            assertThat(labels(targets)).doesNotHaveDuplicates();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UUID type — semantic only
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("UUID type")
    class UuidType {
        @Test
        void uuid_semanticOnly() {
            var f = FieldConstraints.builder("id", java.util.UUID.class).build();
            assertThat(labels(BoundaryPlanner.plan(f))).containsExactly("semantic");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BoundaryTarget — isFixed(), isSemantic()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("BoundaryTarget")
    class BoundaryTargetCoverage {
        @Test
        void semantic_isSemantic() {
            assertThat(BoundaryTarget.semantic().isSemantic()).isTrue();
        }

        @Test
        void atMin_isNotSemantic() {
            assertThat(BoundaryTarget.atMin(1).isSemantic()).isFalse();
        }

        @Test
        void atMin_isFixed() {
            assertThat(BoundaryTarget.atMin(1).isFixed()).isTrue();
        }

        @Test
        void semantic_isNotFixed() {
            assertThat(BoundaryTarget.semantic().isFixed()).isFalse();
        }

        @Test
        void nullTarget_isNotSemantic() {
            assertThat(BoundaryTarget.nullTarget().isSemantic()).isFalse();
        }

        @Test
        void allEnumValues_isNotFixed() {
            assertThat(BoundaryTarget.allEnumValues().isFixed()).isFalse();
        }
    }
}
