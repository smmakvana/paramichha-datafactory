package com.paramichha.datafactory.shaper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StringShaper — format-aware fitting")
class StringShaperTest {

    // ═══════════════════════════════════════════════════════════════════════
    // fitPlain
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("fitPlain")
    class FitPlain {
        @Test
        void exact_unchanged() {
            assertThat(StringShaper.fitPlain("hello", 5)).isEqualTo("hello");
        }

        @Test
        void tooLong_trimmed() {
            assertThat(StringShaper.fitPlain("hello world", 5)).isEqualTo("hello");
        }

        @Test
        void tooShort_padded() {
            assertThat(StringShaper.fitPlain("hi", 5)).isEqualTo("hixxx");
        }

        @Test
        void empty_padded() {
            assertThat(StringShaper.fitPlain("", 3)).isEqualTo("xxx");
        }

        @Test
        void length_correct() {
            assertThat(StringShaper.fitPlain("abc", 10)).hasSize(10);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // fitEmail
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("fitEmail")
    class FitEmail {

        @Test
        void exact_unchanged() {
            String email = "a@b.com"; // 7 chars
            assertThat(StringShaper.fitEmail(email, 7)).isEqualTo(email);
        }

        @Test
        void tooLong_trimsLocalPart() {
            // "john.smith@x.com" trimmed to 10 — domain "@x.com" is 6 chars,
            // so local can be up to 4 chars → "john@x.com" = 10 chars
            String result = StringShaper.fitEmail("john.smith@x.com", 10);
            assertThat(result).hasSize(10);
            assertThat(result).contains("@x.com");
            assertThat(result).matches("[^@]+@.+");
        }

        @Test
        void tooShort_padsLocalPart() {
            String result = StringShaper.fitEmail("a@b.com", 15);
            assertThat(result).hasSize(15);
            assertThat(result).contains("@b.com");
            assertThat(result).matches("[^@]+@.+");
        }

        @Test
        void padsToMin_stillValidFormat() {
            String result = StringShaper.fitEmail("u@example.com", 20);
            assertThat(result).hasSize(20);
            assertThat(result).contains("@example.com");
        }

        @Test
        void trimsToMax_stillValidFormat() {
            String result = StringShaper.fitEmail("verylonglocalpart@example.com", 15);
            assertThat(result).hasSize(15);
            assertThat(result).contains("@example.com");
        }

        @Test
        void noAtSign_treatedAsPlain() {
            // degenerate case — falls back to plain fitting
            String result = StringShaper.fitEmail("notanemail", 5);
            assertThat(result).hasSize(5);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // fitUrl
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("fitUrl")
    class FitUrl {
        @Test
        void tooShort_extended() {
            String result = StringShaper.fitUrl("http://example.com", 30);
            assertThat(result.length()).isGreaterThanOrEqualTo(18);
        }

        @Test
        void tooLong_trimmed() {
            String result = StringShaper.fitUrl("http://example.com/very/long/path", 20);
            assertThat(result).hasSize(20);
        }

        @Test
        void exact_unchanged() {
            String url = "http://example.com/x";
            assertThat(StringShaper.fitUrl(url, url.length())).isEqualTo(url);
        }
    }
}
