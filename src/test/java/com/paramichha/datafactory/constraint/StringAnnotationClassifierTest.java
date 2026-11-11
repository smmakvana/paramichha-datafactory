package com.paramichha.datafactory.constraint;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Classifying annotation text: every branch of the string classifier. */
@DisplayName("StringAnnotationClassifier")
class StringAnnotationClassifierTest {

    @Nested
    @DisplayName("StringAnnotationClassifier — all branches")
    class AnnotationStrings {

        private FieldConstraints fromStr(String type, String... anns) {
            return AnnotationResolver.analyzeFromStrings("f", type, List.of(anns));
        }

        @Test
        void decimalMin() {
            var f = fromStr("Integer", "@DecimalMin(1)");
            // DecimalMin not parsed by StringAnnotationClassifier (only runtime) — no bounds
            // Verify it doesn't crash
            assertThat(f).isNotNull();
        }

        @Test
        void digitsIntegerAndFraction() {
            var f = fromStr("BigDecimal", "@Digits(integer=5, fraction=2)");
            assertThat(f.maxIntegerDigits()).isEqualTo(5);
            assertThat(f.maxFractionDigits()).isEqualTo(2);
        }

        @Test
        void positiveOrZero() {
            assertThat(fromStr("Integer", "@PositiveOrZero").bounds().min()).isEqualTo(0L);
        }

        @Test
        void positiveNotPositiveOrZero() {
            // @Positive without @PositiveOrZero — must not match PositiveOrZero branch
            assertThat(fromStr("Integer", "@Positive").bounds().min()).isEqualTo(1L);
        }

        @Test
        void negativeOrZero() {
            assertThat(fromStr("Integer", "@NegativeOrZero").bounds().max()).isEqualTo(0L);
        }

        @Test
        void negativeNotNegativeOrZero() {
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
        void patternExtractsRegexp() {
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
    }

    @Nested
    @DisplayName("StringAnnotationClassifier — branch gaps")
    class BranchGaps {

        private final TextAnnotationReader C = TextAnnotationReader.INSTANCE;

        @Test
        void classifyFormat_isbn()  { assertThat(C.classifyFormat("@ISBN")).isEqualTo(FormatType.ISBN); }
        @Test
        void classifyFormat_ean()   { assertThat(C.classifyFormat("@EAN")).isEqualTo(FormatType.EAN); }
        @Test
        void classifyFormat_none()  { assertThat(C.classifyFormat("@NotNull")).isEqualTo(FormatType.NONE); }

        @Test
        void extractPatternRegexp_no_regexp_keyword_returns_null() {
            assertThat(C.extractPatternRegexp("@NotBlank")).isNull();
        }
        @Test
        void extractPatternRegexp_no_opening_quote_returns_null() {
            assertThat(C.extractPatternRegexp("@Pattern(regexp=noQuote)")).isNull();
        }
        @Test
        void extractPatternRegexp_no_closing_quote_returns_null() {
            assertThat(C.extractPatternRegexp("@Pattern(regexp=\"noclose")).isNull();
        }
        @Test
        void extractPatternRegexp_valid() {
            assertThat(C.extractPatternRegexp("@Pattern(regexp=\"^[A-Z]+$\")")).isEqualTo("^[A-Z]+$");
        }

        @Test
        void classifyQuantity_min_no_digits_returns_null()  { assertThat(C.classifyQuantity("@Min(abc)")).isNull(); }
        @Test
        void classifyQuantity_max_no_digits_returns_null()  { assertThat(C.classifyQuantity("@Max(abc)")).isNull(); }
        @Test
        void classifyQuantity_size_both_trimmed_returns_null() {
            assertThat(C.classifyQuantity("@Size(min=0, max=2147483647)")).isNull();
        }
        @Test
        void classifyQuantity_length_both_trimmed_returns_null() {
            assertThat(C.classifyQuantity("@Length(min=0, max=2147483647)")).isNull();
        }
        @Test
        void classifyQuantity_min_returns_bounds() {
            var b = C.classifyQuantity("@Min(18)");
            assertThat(b).isNotNull();
            assertThat(b.min()).isEqualTo(18L);
        }
        @Test
        void classifyQuantity_max_returns_bounds() {
            var b = C.classifyQuantity("@Max(100)");
            assertThat(b).isNotNull();
            assertThat(b.max()).isEqualTo(100L);
        }

        @Test
        void extractLong_negative_value_parsed() {
            var b = C.classifyQuantity("@Min(-5)");
            assertThat(b).isNotNull();
            assertThat(b.min()).isEqualTo(-5L);
        }
        @Test
        void extractLong_negative_max_parsed() {
            var b = C.classifyQuantity("@Max(-1)");
            assertThat(b).isNotNull();
            assertThat(b.max()).isEqualTo(-1L);
        }
        @Test
        void extractLong_empty_returns_null() {
            assertThat(C.classifyQuantity("@Min()")).isNull();
        }

        @Test
        void classifyTemporal_none()    { assertThat(C.classifyTemporal("@NotNull")).isEqualTo(TemporalDirection.NONE); }
        @Test
        void classifyTemporal_past()    { assertThat(C.classifyTemporal("@Past")).isEqualTo(TemporalDirection.PAST); }
        @Test
        void classifyTemporal_future()  { assertThat(C.classifyTemporal("@Future")).isEqualTo(TemporalDirection.FUTURE); }
    }
}
