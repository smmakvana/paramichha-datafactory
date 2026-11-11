package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static com.paramichha.datafactory.generation.ShaperTestSupport.*;
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

    @Nested
    @DisplayName("NumericShaper — type dispatch")
    class TypeDispatch {

        @Test
        void integer() {
            var f = field("age", Integer.class, "@Min(18)", "@Max(120)");
            var v = NumericDataCreator.getNumber(f, atMin(18), DataMachines.random());
            assertThat(v).isInstanceOf(Integer.class).isEqualTo(18);
        }

        @Test
        void intPrimitive() {
            var f = AnnotationResolver.analyzeFromStrings("n", "int", List.of("@Min(1)"));
            var v = NumericDataCreator.getNumber(f, atMin(1), DataMachines.random());
            assertThat(v).isInstanceOf(Integer.class);
        }

        @Test
        void longType() {
            var f = AnnotationResolver.analyzeFromStrings("amount", "Long", List.of("@Min(100)"));
            var v = NumericDataCreator.getNumber(f, atMin(100), DataMachines.random());
            assertThat(v).isInstanceOf(Long.class).isEqualTo(100L);
        }

        @Test
        void longPrimitive() {
            var f = AnnotationResolver.analyzeFromStrings("n", "long", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(Long.class);
        }

        @Test
        void shortType() {
            var f = AnnotationResolver.analyzeFromStrings("n", "Short", List.of("@Min(1)"));
            var v = NumericDataCreator.getNumber(f, atMin(1), DataMachines.random());
            assertThat(v).isInstanceOf(Short.class);
        }

        @Test
        void shortPrimitive() {
            var f = AnnotationResolver.analyzeFromStrings("n", "short", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(Short.class);
        }

        @Test
        void byteType() {
            var f = AnnotationResolver.analyzeFromStrings("n", "Byte", List.of("@Min(1)"));
            var v = NumericDataCreator.getNumber(f, atMin(1), DataMachines.random());
            assertThat(v).isInstanceOf(Byte.class);
        }

        @Test
        void bytePrimitive() {
            var f = AnnotationResolver.analyzeFromStrings("n", "byte", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(Byte.class);
        }

        @Test
        void doubleType() {
            var f = AnnotationResolver.analyzeFromStrings("price", "Double", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(Double.class);
        }

        @Test
        void doublePrimitive() {
            var f = AnnotationResolver.analyzeFromStrings("n", "double", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(Double.class);
        }

        @Test
        void floatType() {
            var f = AnnotationResolver.analyzeFromStrings("n", "Float", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(Float.class);
        }

        @Test
        void floatPrimitive() {
            var f = AnnotationResolver.analyzeFromStrings("n", "float", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(Float.class);
        }

        @Test
        void bigDecimal() {
            var f = AnnotationResolver.analyzeFromStrings("price", "BigDecimal", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(BigDecimal.class);
        }

        @Test
        void bigDecimal_withTarget() {
            var f = AnnotationResolver.analyzeFromStrings("price", "BigDecimal", List.of("@Min(5)"));
            var v = NumericDataCreator.getNumber(f, atMin(5), DataMachines.random());
            assertThat(v).isInstanceOf(BigDecimal.class);
        }

        @Test
        void bigInteger() {
            var f = AnnotationResolver.analyzeFromStrings("n", "BigInteger", List.of());
            var v = NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isInstanceOf(BigInteger.class);
        }
    }

    @Nested
    @DisplayName("NumericShaper — semantic value branches")
    class SemanticValues {

        @Test
        void ageHint() {
            var f = fieldOf("age", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(30);
        }

        @Test
        void yearHint() {
            var f = fieldOf("year", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(2024);
        }

        @Test
        void countHint() {
            var f = fieldOf("count", Integer.class);
            assertThat((Integer) NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isGreaterThan(0);
        }

        @Test
        void totalHint() {
            var f = fieldOf("total", Integer.class);
            assertThat((Integer) NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isGreaterThan(0);
        }

        @Test
        void pageHint() {
            var f = fieldOf("page", Integer.class);
            assertThat((Integer) NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isPositive();
        }

        @Test
        void retryHint() {
            var f = fieldOf("retryCount", Integer.class);
            assertThat((Integer) NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isNotNegative();
        }

        @Test
        void attemptHint() {
            var f = fieldOf("attempts", Integer.class);
            assertThat((Integer) NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isNotNegative();
        }

        @Test
        void ratingHint() {
            var f = fieldOf("rating", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(5);
        }

        @Test
        void scoreHint() {
            var f = fieldOf("score", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(5);
        }

        @Test
        void priceHint() {
            var f = fieldOf("price", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(100);
        }

        @Test
        void amountHint() {
            var f = fieldOf("amount", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(100);
        }

        @Test
        void quantityHint() {
            var f = fieldOf("quantity", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(1);
        }

        @Test
        void qtyHint() {
            var f = fieldOf("qty", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(1);
        }

        @Test
        void minOnly_returnsMinPlusOne() {
            var f = field("n", Integer.class, "@Min(10)");
            assertThat((Integer) NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(11);
        }

        @Test
        void maxOnly_returnsMaxMinusOne() {
            var f = field("n", Integer.class, "@Max(100)");
            assertThat((Integer) NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isEqualTo(99);
        }

        @Test
        void noBounds_noHint_returnsFakerValue() {
            var f = fieldOf("value", Integer.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isInstanceOf(Integer.class);
        }

        @Test
        void doubleMinMaxMidpoint() {
            var f = field("n", Double.class, "@Min(0)", "@Max(10)");
            double v = (double) NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isEqualTo(5.0);
        }

        @Test
        void doubleMinOnly() {
            var f = field("n", Double.class, "@Min(2)");
            double v = (double) NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isEqualTo(2.5);
        }

        @Test
        void doubleMaxOnly() {
            var f = field("n", Double.class, "@Max(10)");
            double v = (double) NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v).isEqualTo(9.5);
        }

        @Test
        void doubleNoBoundsFaker() {
            var f = fieldOf("n", Double.class);
            assertThat(NumericDataCreator.getNumber(f, semantic(), DataMachines.random())).isInstanceOf(Double.class);
        }

        @Test
        void bigDecimal_fractionDigits() {
            var f = AnnotationResolver.analyzeFromStrings("price", "BigDecimal",
                    List.of("@Digits(integer=5, fraction=2)"));
            var v = (BigDecimal) NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v.scale()).isEqualTo(2);
        }

        @Test
        void bigDecimal_noFractionDigits_defaultsToTwo() {
            var f = fieldOf("price", BigDecimal.class);
            var v = (BigDecimal) NumericDataCreator.getNumber(f, semantic(), DataMachines.random());
            assertThat(v.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("NumericShaper — double with fixed target")
    class DoubleWithFixedTarget {

        @Test
        void doubleAtMinTarget() {
            var f = AnnotationResolver.analyzeFromStrings("n", "Double", List.of("@Min(1)", "@Max(10)"));
            double v = (double) NumericDataCreator.getNumber(f, atMin(1), DataMachines.random());
            assertThat(v).isEqualTo(1.0d);
        }

        @Test
        void bigDecimal_withDigits_atMin() {
            var f = AnnotationResolver.analyzeFromStrings("price", "BigDecimal",
                    List.of("@Digits(integer=5, fraction=2)"));
            var v = (java.math.BigDecimal) NumericDataCreator.getNumber(f, atMin(5), DataMachines.random());
            assertThat(v.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("NumericTypeShaper")
    class TypeShaperDelegation {
        @Test
        void supportsInteger() {
            assertThat(NumericDataCreator.INSTANCE.supports(Integer.class)).isTrue();
        }

        @Test
        void supportsLong() {
            assertThat(NumericDataCreator.INSTANCE.supports(long.class)).isTrue();
        }

        @Test
        void supportsDouble() {
            assertThat(NumericDataCreator.INSTANCE.supports(double.class)).isTrue();
        }

        @Test
        void supportsFloat() {
            assertThat(NumericDataCreator.INSTANCE.supports(float.class)).isTrue();
        }

        @Test
        void supportsShort() {
            assertThat(NumericDataCreator.INSTANCE.supports(short.class)).isTrue();
        }

        @Test
        void supportsByte() {
            assertThat(NumericDataCreator.INSTANCE.supports(byte.class)).isTrue();
        }

        @Test
        void supportsBigDec() {
            assertThat(NumericDataCreator.INSTANCE.supports(BigDecimal.class)).isTrue();
        }

        @Test
        void supportsBigInt() {
            assertThat(NumericDataCreator.INSTANCE.supports(BigInteger.class)).isTrue();
        }

        @Test
        void supportsStringFalse() {
            assertThat(NumericDataCreator.INSTANCE.supports(String.class)).isFalse();
        }

        @Test
        void shapeDelegates() {
            var f = field("n", Integer.class, "@Min(1)");
            assertThat(NumericDataCreator.INSTANCE.shape(f, atMin(1), DataMachines.random())).isEqualTo(1);
        }
    }
}
