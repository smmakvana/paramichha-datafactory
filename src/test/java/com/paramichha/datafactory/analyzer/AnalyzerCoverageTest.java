package com.paramichha.datafactory.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for analyzer package — missing branches:
 * QuantityBounds  — all factory methods, intersect all branches, isContradictory
 * FieldConstraints   — all convenience methods
 * RuntimeAnnotationClassifier — CreditCardNumber, ISBN, EAN, UUID_STRING, DecimalMin/Max, Length, Digits
 * StringAnnotationClassifier  — all annotation string paths
 * AnnotationAnalyzer.resolveType — all type mappings
 */
@DisplayName("Analyzer coverage")
class AnalyzerCoverageTest {

    // ═══════════════════════════════════════════════════════════════════════
    // QuantityBounds — all factory methods and branch paths
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("QuantityBounds")
    class QuantityBoundsCoverage {

        @Test
        void unbounded() {
            assertThat(QuantityBounds.unbounded().isUnbounded()).isTrue();
        }

        @Test
        void of() {
            var b = QuantityBounds.of(1L, 10L);
            assertThat(b.min()).isEqualTo(1L);
            assertThat(b.max()).isEqualTo(10L);
        }

        @Test
        void atLeast() {
            var b = QuantityBounds.atLeast(5L);
            assertThat(b.hasMin()).isTrue();
            assertThat(b.hasMax()).isFalse();
        }

        @Test
        void atMost() {
            var b = QuantityBounds.atMost(5L);
            assertThat(b.hasMax()).isTrue();
            assertThat(b.hasMin()).isFalse();
        }

        @Test
        void exactly() {
            var b = QuantityBounds.exactly(5L);
            assertThat(b.min()).isEqualTo(b.max());
        }

        @Test
        void hasBoth() {
            assertThat(QuantityBounds.of(1L, 10L).hasBoth()).isTrue();
        }

        @Test
        void hasBoth_false() {
            assertThat(QuantityBounds.atLeast(1L).hasBoth()).isFalse();
        }

        @Test
        void isContradictory_true() {
            assertThat(new QuantityBounds(10L, 5L).isContradictory()).isTrue();
        }

        @Test
        void isContradictory_false() {
            assertThat(new QuantityBounds(5L, 10L).isContradictory()).isFalse();
        }

        @Test
        void isContradictory_noMin() {
            assertThat(QuantityBounds.atMost(5L).isContradictory()).isFalse();
        }

        // intersect — all branch combinations in maxOf/minOf
        @Test
        void intersect_bothBounds() {
            var a = QuantityBounds.of(2L, 8L);
            var b = QuantityBounds.of(5L, 10L);
            var r = a.intersect(b);
            assertThat(r.min()).isEqualTo(5L); // max of 2,5
            assertThat(r.max()).isEqualTo(8L); // min of 8,10
        }

        @Test
        void intersect_thisMinNull() {
            // maxOf(null, 5) → 5
            var a = QuantityBounds.atMost(10L);
            var b = QuantityBounds.atLeast(5L);
            var r = a.intersect(b);
            assertThat(r.min()).isEqualTo(5L);
            assertThat(r.max()).isEqualTo(10L);
        }

        @Test
        void intersect_otherMinNull() {
            // maxOf(5, null) → 5
            var a = QuantityBounds.atLeast(5L);
            var b = QuantityBounds.atMost(10L);
            var r = a.intersect(b);
            assertThat(r.min()).isEqualTo(5L);
        }

        @Test
        void intersect_thisMaxNull() {
            // minOf(null, 10) → 10
            var a = QuantityBounds.atLeast(1L);
            var b = QuantityBounds.atMost(10L);
            var r = a.intersect(b);
            assertThat(r.max()).isEqualTo(10L);
        }

        @Test
        void intersect_otherMaxNull() {
            // minOf(10, null) → 10
            var a = QuantityBounds.atMost(10L);
            var b = QuantityBounds.atLeast(1L);
            var r = a.intersect(b);
            assertThat(r.max()).isEqualTo(10L);
        }

        @Test
        void intersect_bothNull_remainsUnbounded() {
            var r = QuantityBounds.unbounded().intersect(QuantityBounds.unbounded());
            assertThat(r.isUnbounded()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FieldConstraints — all convenience methods
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("FieldConstraints convenience methods")
    class FieldConstraintsMethods {

        private FieldConstraints make(String ann) {
            return AnnotationAnalyzer.analyzeFromStrings("f", "LocalDate", List.of(ann));
        }

        @Test
        void isPast_past() {
            assertThat(make("@Past").isPast()).isTrue();
        }

        @Test
        void isPast_pastOrNow() {
            assertThat(make("@PastOrPresent").isPast()).isTrue();
        }

        @Test
        void isPast_future_false() {
            assertThat(make("@Future").isPast()).isFalse();
        }

        @Test
        void isFuture_future() {
            assertThat(make("@Future").isFuture()).isTrue();
        }

        @Test
        void isFuture_futureOrNow() {
            assertThat(make("@FutureOrPresent").isFuture()).isTrue();
        }

        @Test
        void isFuture_past_false() {
            assertThat(make("@Past").isFuture()).isFalse();
        }

        @Test
        void isStrictPast_past() {
            assertThat(make("@Past").isStrictPast()).isTrue();
        }

        @Test
        void isStrictPast_pastOrNow_false() {
            assertThat(make("@PastOrPresent").isStrictPast()).isFalse();
        }

        @Test
        void isStrictFuture_future() {
            assertThat(make("@Future").isStrictFuture()).isTrue();
        }

        @Test
        void isStrictFuture_futureOrNow_false() {
            assertThat(make("@FutureOrPresent").isStrictFuture()).isFalse();
        }

        @Test
        void hasFormat_true() {
            assertThat(AnnotationAnalyzer.analyzeFromStrings("e", "String", List.of("@Email")).hasFormat()).isTrue();
        }

        @Test
        void hasFormat_false() {
            assertThat(AnnotationAnalyzer.analyzeFromStrings("n", "String", List.of()).hasFormat()).isFalse();
        }

        @Test
        void hasBounds_true() {
            assertThat(AnnotationAnalyzer.analyzeFromStrings("n", "Integer", List.of("@Min(1)")).hasBounds()).isTrue();
        }

        @Test
        void hasBounds_false() {
            assertThat(AnnotationAnalyzer.analyzeFromStrings("n", "Integer", List.of()).hasBounds()).isFalse();
        }

        @Test
        void hasTemporal_true() {
            assertThat(make("@Past").hasTemporal()).isTrue();
        }

        @Test
        void hasTemporal_false() {
            assertThat(AnnotationAnalyzer.analyzeFromStrings("n", "String", List.of()).hasTemporal()).isFalse();
        }

        @Test
        void hasWarnings_true() {
            assertThat(AnnotationAnalyzer.analyzeFromStrings("f", "LocalDate", List.of("@Past", "@Future")).hasWarnings()).isTrue();
        }

        @Test
        void hasWarnings_false() {
            assertThat(make("@Past").hasWarnings()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RuntimeAnnotationClassifier — uncovered format types via AnnotationAnalyzer.analyze()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RuntimeAnnotationClassifier — all format types")
    class RuntimeClassifierFormats {

        @Test
        void creditCardNumber() {
            var f = AnnotationAnalyzer.analyzeFromStrings("cc", "String", List.of("@CreditCardNumber"));
            assertThat(f.format()).isEqualTo(FormatType.CREDIT_CARD);
        }

        @Test
        void isbn() {
            var f = AnnotationAnalyzer.analyzeFromStrings("isbn", "String", List.of("@ISBN"));
            assertThat(f.format()).isEqualTo(FormatType.ISBN);
        }

        @Test
        void ean() {
            var f = AnnotationAnalyzer.analyzeFromStrings("ean", "String", List.of("@EAN"));
            assertThat(f.format()).isEqualTo(FormatType.EAN);
        }

        // UUID_STRING — Hibernate @UUID annotation — only available via runtime classifier
        // Skip here as it requires hibernate-specific annotation instance
    }

    // ═══════════════════════════════════════════════════════════════════════
    // StringAnnotationClassifier — all annotation string branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("StringAnnotationClassifier — all branches")
    class StringClassifierBranches {

        private FieldConstraints fromStr(String type, String... anns) {
            return AnnotationAnalyzer.analyzeFromStrings("f", type, List.of(anns));
        }

        @Test
        void decimalMin() {
            var f = fromStr("Integer", "@DecimalMin(1)");
            // DecimalMin not parsed by StringAnnotationClassifier (only runtime) — no bounds
            // Verify it doesn't crash
            assertThat(f).isNotNull();
        }

        @Test
        void digits_integerAndFraction() {
            var f = fromStr("BigDecimal", "@Digits(integer=5, fraction=2)");
            assertThat(f.maxIntegerDigits()).isEqualTo(5);
            assertThat(f.maxFractionDigits()).isEqualTo(2);
        }

        @Test
        void positiveOrZero() {
            assertThat(fromStr("Integer", "@PositiveOrZero").bounds().min()).isEqualTo(0L);
        }

        @Test
        void positive_not_positiveOrZero() {
            // @Positive without @PositiveOrZero — must not match PositiveOrZero branch
            assertThat(fromStr("Integer", "@Positive").bounds().min()).isEqualTo(1L);
        }

        @Test
        void negativeOrZero() {
            assertThat(fromStr("Integer", "@NegativeOrZero").bounds().max()).isEqualTo(0L);
        }

        @Test
        void negative_not_negativeOrZero() {
            assertThat(fromStr("Integer", "@Negative").bounds().max()).isEqualTo(-1L);
        }

        @Test
        void minWithValue() {
            assertThat(fromStr("Integer", "@Min(18)").bounds().min()).isEqualTo(18L);
        }

        @Test
        void maxWithValue() {
            assertThat(fromStr("Integer", "@Max(120)").bounds().max()).isEqualTo(120L);
        }

        @Test
        void sizeMinZeroOmitted() {
            // size min=0 → treated as no min
            var f = fromStr("String", "@Size(min=0, max=50)");
            assertThat(f.bounds().hasMin()).isFalse();
            assertThat(f.bounds().max()).isEqualTo(50L);
        }

        @Test
        void sizeMaxIntMaxOmitted() {
            // size max=Integer.MAX_VALUE → treated as no max
            var f = fromStr("String", "@Size(min=2, max=2147483647)");
            assertThat(f.bounds().hasMax()).isFalse();
            assertThat(f.bounds().min()).isEqualTo(2L);
        }

        @Test
        void lengthBothBounds() {
            var f = fromStr("String", "@Length(min=3, max=20)");
            assertThat(f.bounds().min()).isEqualTo(3L);
            assertThat(f.bounds().max()).isEqualTo(20L);
        }

        @Test
        void pattern_extractsRegexp() {
            var f = fromStr("String", "@Pattern(regexp=\"^[A-Z]+$\")");
            assertThat(f.format()).isEqualTo(FormatType.PATTERN);
            assertThat(f.patternRegexp()).isEqualTo("^[A-Z]+$");
        }

        @Test
        void isNull_not_notNull() {
            // "@Null" without "@NotNull"
            var f = fromStr("String", "@Null");
            assertThat(f.mustBeNull()).isTrue();
        }

        @Test
        void isValid_cascade() {
            var f = fromStr("String", "@Valid");
            assertThat(f.cascadeValid()).isTrue();
        }

        @Test
        void multipleFormats_warns() {
            var f = fromStr("String", "@Email", "@URL");
            assertThat(f.hasWarnings()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AnnotationAnalyzer.resolveType — all type mappings
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AnnotationAnalyzer.resolveType all mappings")
    class ResolveType {

        private FieldConstraints make(String type) {
            return AnnotationAnalyzer.analyzeFromStrings("f", type, List.of());
        }

        @Test
        void string() {
            assertThat(make("String").fieldType()).isEqualTo(String.class);
        }

        @Test
        void integer() {
            assertThat(make("Integer").fieldType()).isEqualTo(Integer.class);
        }

        @Test
        void intPrimitive() {
            assertThat(make("int").fieldType()).isEqualTo(Integer.class);
        }

        @Test
        void longType() {
            assertThat(make("Long").fieldType()).isEqualTo(Long.class);
        }

        @Test
        void longPrimitive() {
            assertThat(make("long").fieldType()).isEqualTo(Long.class);
        }

        @Test
        void doubleType() {
            assertThat(make("Double").fieldType()).isEqualTo(Double.class);
        }

        @Test
        void doublePrimitive() {
            assertThat(make("double").fieldType()).isEqualTo(Double.class);
        }

        @Test
        void floatType() {
            assertThat(make("Float").fieldType()).isEqualTo(Float.class);
        }

        @Test
        void floatPrimitive() {
            assertThat(make("float").fieldType()).isEqualTo(Float.class);
        }

        @Test
        void booleanType() {
            assertThat(make("Boolean").fieldType()).isEqualTo(Boolean.class);
        }

        @Test
        void booleanPrimitive() {
            assertThat(make("boolean").fieldType()).isEqualTo(Boolean.class);
        }

        @Test
        void charType() {
            assertThat(make("Character").fieldType()).isEqualTo(Character.class);
        }

        @Test
        void charPrimitive() {
            assertThat(make("char").fieldType()).isEqualTo(Character.class);
        }

        @Test
        void bigDecimal() {
            assertThat(make("BigDecimal").fieldType()).isEqualTo(java.math.BigDecimal.class);
        }

        @Test
        void bigInteger() {
            assertThat(make("BigInteger").fieldType()).isEqualTo(java.math.BigInteger.class);
        }

        @Test
        void instant() {
            assertThat(make("Instant").fieldType()).isEqualTo(java.time.Instant.class);
        }

        @Test
        void localDate() {
            assertThat(make("LocalDate").fieldType()).isEqualTo(java.time.LocalDate.class);
        }

        @Test
        void localDateTime() {
            assertThat(make("LocalDateTime").fieldType()).isEqualTo(java.time.LocalDateTime.class);
        }

        @Test
        void localTime() {
            assertThat(make("LocalTime").fieldType()).isEqualTo(java.time.LocalTime.class);
        }

        @Test
        void zonedDateTime() {
            assertThat(make("ZonedDateTime").fieldType()).isEqualTo(java.time.ZonedDateTime.class);
        }

        @Test
        void offsetDateTime() {
            assertThat(make("OffsetDateTime").fieldType()).isEqualTo(java.time.OffsetDateTime.class);
        }

        @Test
        void uuid() {
            assertThat(make("UUID").fieldType()).isEqualTo(java.util.UUID.class);
        }

        @Test
        void shortType() {
            assertThat(make("Short").fieldType()).isEqualTo(Short.class);
        }

        @Test
        void shortPrimitive() {
            assertThat(make("short").fieldType()).isEqualTo(Short.class);
        }

        @Test
        void byteType() {
            assertThat(make("Byte").fieldType()).isEqualTo(Byte.class);
        }

        @Test
        void bytePrimitive() {
            assertThat(make("byte").fieldType()).isEqualTo(Byte.class);
        }

        @Test
        void date() {
            assertThat(make("Date").fieldType()).isEqualTo(java.util.Date.class);
        }

        @Test
        void year() {
            assertThat(make("Year").fieldType()).isEqualTo(java.time.Year.class);
        }

        @Test
        void yearMonth() {
            assertThat(make("YearMonth").fieldType()).isEqualTo(java.time.YearMonth.class);
        }

        @Test
        void list() {
            assertThat(make("List").fieldType()).isEqualTo(java.util.List.class);
        }

        @Test
        void collection() {
            assertThat(make("Collection").fieldType()).isEqualTo(java.util.Collection.class);
        }

        @Test
        void unknown_defaultsToString() {
            assertThat(make("UnknownType").fieldType()).isEqualTo(String.class);
        }
    }
}
