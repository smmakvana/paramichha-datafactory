package com.paramichha.datafactory.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers every missing branch in StringAnnotationClassifier:
 * classifyFormat    — @ISBN, @EAN
 * extractPatternRegexp — idx<0, q1<0, q2<0
 * classifyQuantity  — Min null result, Max null result, Size/Length both null after trim
 * extractLong       — negative number path, NumberFormatException path
 * classifyTemporal  — NONE return
 */
@DisplayName("StringAnnotationClassifier — branch coverage gaps")
class StringClassifierGapTest {

    private static final StringAnnotationClassifier C = StringAnnotationClassifier.INSTANCE;

    // ── classifyFormat ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("classifyFormat — ISBN and EAN")
    class ClassifyFormat {
        @Test
        void isbn() {
            assertThat(C.classifyFormat("@ISBN")).isEqualTo(FormatType.ISBN);
        }

        @Test
        void ean() {
            assertThat(C.classifyFormat("@EAN")).isEqualTo(FormatType.EAN);
        }

        @Test
        void none() {
            assertThat(C.classifyFormat("@NotNull")).isEqualTo(FormatType.NONE);
        }
    }

    // ── extractPatternRegexp ───────────────────────────────────────────────

    @Nested
    @DisplayName("extractPatternRegexp — null paths")
    class ExtractPatternRegexp {

        @Test
        void no_regexp_keyword_returns_null() {
            // "idx < 0" — annotation has no "regexp" in it
            assertThat(C.extractPatternRegexp("@NotBlank")).isNull();
        }

        @Test
        void regexp_no_opening_quote_returns_null() {
            // "q1 < 0" — has "regexp" but no '"' after it
            assertThat(C.extractPatternRegexp("@Pattern(regexp=noQuote)")).isNull();
        }

        @Test
        void regexp_no_closing_quote_returns_null() {
            // "q2 < 0" — has opening '"' but no closing '"'
            assertThat(C.extractPatternRegexp("@Pattern(regexp=\"noclose")).isNull();
        }

        @Test
        void valid_regexp_extracted() {
            assertThat(C.extractPatternRegexp("@Pattern(regexp=\"^[A-Z]+$\")"))
                    .isEqualTo("^[A-Z]+$");
        }
    }

    // ── classifyQuantity ──────────────────────────────────────────────────

    @Nested
    @DisplayName("classifyQuantity — null extractLong results and Size/Length both null")
    class ClassifyQuantity {

        @Test
        void min_with_no_digits_returns_null() {
            // "Min(" present but extractLong returns null (no digits after prefix)
            assertThat(C.classifyQuantity("@Min(abc)")).isNull();
        }

        @Test
        void max_with_no_digits_returns_null() {
            assertThat(C.classifyQuantity("@Max(abc)")).isNull();
        }

        @Test
        void size_zero_min_and_maxint_max_both_trimmed_returns_null() {
            // min=0 → trimmed to null, max=Integer.MAX_VALUE → trimmed to null → neither non-null → return null
            assertThat(C.classifyQuantity("@Size(min=0, max=2147483647)")).isNull();
        }

        @Test
        void length_zero_min_and_maxint_max_returns_null() {
            assertThat(C.classifyQuantity("@Length(min=0, max=2147483647)")).isNull();
        }

        @Test
        void min_with_value_returns_bounds() {
            var b = C.classifyQuantity("@Min(18)");
            assertThat(b).isNotNull();
            assertThat(b.min()).isEqualTo(18L);
        }

        @Test
        void max_with_value_returns_bounds() {
            var b = C.classifyQuantity("@Max(100)");
            assertThat(b).isNotNull();
            assertThat(b.max()).isEqualTo(100L);
        }
    }

    // ── extractLong ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractLong — negative and NumberFormatException paths")
    class ExtractLong {

        @Test
        void negative_value_parsed() {
            // s.charAt(end) == '-' branch — @Min(-5)
            var b = C.classifyQuantity("@Min(-5)");
            assertThat(b).isNotNull();
            assertThat(b.min()).isEqualTo(-5L);
        }

        @Test
        void negative_max_parsed() {
            var b = C.classifyQuantity("@Max(-1)");
            assertThat(b).isNotNull();
            assertThat(b.max()).isEqualTo(-1L);
        }

        @Test
        void empty_after_prefix_returns_null() {
            // start == end immediately — Long.parseLong("") throws NumberFormatException
            assertThat(C.classifyQuantity("@Min()")).isNull();
        }
    }

    // ── classifyTemporal ───────────────────────────────────────────────────

    @Nested
    @DisplayName("classifyTemporal — NONE return")
    class ClassifyTemporal {

        @Test
        void non_temporal_annotation_returns_none() {
            assertThat(C.classifyTemporal("@NotNull")).isEqualTo(TemporalDirection.NONE);
        }

        @Test
        void past_returns_past() {
            assertThat(C.classifyTemporal("@Past")).isEqualTo(TemporalDirection.PAST);
        }

        @Test
        void future_returns_future() {
            assertThat(C.classifyTemporal("@Future")).isEqualTo(TemporalDirection.FUTURE);
        }
    }
}
