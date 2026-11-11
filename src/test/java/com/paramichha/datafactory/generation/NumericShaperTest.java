package com.paramichha.datafactory.generation;

import java.util.List;
import com.paramichha.datafactory.constraint.AnnotationResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The numeric shaper: bounded values, midpoints, and the quantity hint. */
@DisplayName("NumericShaper")
class NumericShaperTest {

    @Nested
    @DisplayName("NumericShaper — remaining semantic branches")
    class SemanticBranches {

        // semanticValue: min != null && max != null -> midpoint (line 53)
        // This path is hit when NO field-name hint matches AND both bounds present
        @Test
        void bothBoundsGiveTheMidpointWithoutAHint() {
            var fc = AnnotationResolver.analyzeFromStrings("zzz", "Integer",
                    List.of("@Min(10)", "@Max(20)"));
            var v = (Integer) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isEqualTo(15); // (10+20)/2
        }

        // semanticValue: min only (line 54) — no hint, min present, no max
        @Test
        void minOnlyWithoutAHint() {
            var fc = AnnotationResolver.analyzeFromStrings("zzz", "Integer", List.of("@Min(7)"));
            var v = (Integer) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isEqualTo(8); // min + 1
        }

        // semanticValue: max only (line 55) — no hint, max present, no min
        @Test
        void maxOnlyWithoutAHint() {
            var fc = AnnotationResolver.analyzeFromStrings("zzz", "Integer", List.of("@Max(50)"));
            var v = (Integer) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isEqualTo(49); // max - 1
        }
    }

    @Nested
    @DisplayName("NumericShaper — bounded() and shapeDouble() branches")
    class BoundedAndDoubleValues {

        private Object shape(String name, String type, String... anns) {
            var fc = AnnotationResolver.analyzeFromStrings(name, type, List.of(anns));
            return NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
        }

        // bounded() — preferred < min → result clamped up to min
        @Test
        void boundedClampsToMin() {
            // "age" hint prefers 30, but min=50 → result must be >= 50
            var fc = AnnotationResolver.analyzeFromStrings("age", "Integer", List.of("@Min(50)", "@Max(200)"));
            var v = (Integer) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isGreaterThanOrEqualTo(50);
        }

        // bounded() — preferred > max → result clamped down to max
        @Test
        void boundedClampsToMax() {
            // "price" hint prefers 100, but max=5 → result must be <= 5
            var fc = AnnotationResolver.analyzeFromStrings("price", "Integer", List.of("@Min(1)", "@Max(5)"));
            var v = (Integer) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isLessThanOrEqualTo(5);
        }

        // shapeDouble — both min and max → midpoint
        @Test
        void doubleWithBothBoundsGivesTheMidpoint() {
            var fc = AnnotationResolver.analyzeFromStrings("n", "Double", List.of("@Min(10)", "@Max(20)"));
            var v = (Double) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isEqualTo(15.0d);
        }

        // shapeDouble — min only → min + 0.5
        @Test
        void doubleWithMinOnly() {
            var fc = AnnotationResolver.analyzeFromStrings("n", "Double", List.of("@Min(10)"));
            var v = (Double) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isEqualTo(10.5d);
        }

        // shapeDouble — max only → max - 0.5
        @Test
        void doubleWithMaxOnly() {
            var fc = AnnotationResolver.analyzeFromStrings("n", "Double", List.of("@Max(20)"));
            var v = (Double) NumericDataCreator.getNumber(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v).isEqualTo(19.5d);
        }

        // semanticValue — quantity/qty hint
        @Test
        void quantityHintIsUsed() {
            assertThat((Integer) shape("quantity", "Integer")).isEqualTo(1);
        }
    }
}
