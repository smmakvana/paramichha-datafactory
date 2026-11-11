package com.paramichha.datafactory.generation;

import java.util.List;
import com.paramichha.datafactory.constraint.AnnotationResolver;
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
            assertThat(StringDataCreator.fitPlain("hello", 5)).isEqualTo("hello");
        }

        @Test
        void tooLong_trimmed() {
            assertThat(StringDataCreator.fitPlain("hello world", 5)).isEqualTo("hello");
        }

        @Test
        void tooShort_padded() {
            assertThat(StringDataCreator.fitPlain("hi", 5)).isEqualTo("hixxx");
        }

        @Test
        void empty_padded() {
            assertThat(StringDataCreator.fitPlain("", 3)).isEqualTo("xxx");
        }

        @Test
        void length_correct() {
            assertThat(StringDataCreator.fitPlain("abc", 10)).hasSize(10);
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
            assertThat(StringDataCreator.fitEmail(email, 7)).isEqualTo(email);
        }

        @Test
        void tooLong_trimsLocalPart() {
            // "john.smith@x.com" trimmed to 10 — domain "@x.com" is 6 chars,
            // so local can be up to 4 chars → "john@x.com" = 10 chars
            String result = StringDataCreator.fitEmail("john.smith@x.com", 10);
            assertThat(result).hasSize(10);
            assertThat(result).contains("@x.com");
            assertThat(result).matches("[^@]+@.+");
        }

        @Test
        void tooShort_padsLocalPart() {
            String result = StringDataCreator.fitEmail("a@b.com", 15);
            assertThat(result).hasSize(15);
            assertThat(result).contains("@b.com");
            assertThat(result).matches("[^@]+@.+");
        }

        @Test
        void padsToMin_stillValidFormat() {
            String result = StringDataCreator.fitEmail("u@example.com", 20);
            assertThat(result).hasSize(20);
            assertThat(result).contains("@example.com");
        }

        @Test
        void trimsToMax_stillValidFormat() {
            String result = StringDataCreator.fitEmail("verylonglocalpart@example.com", 15);
            assertThat(result).hasSize(15);
            assertThat(result).contains("@example.com");
        }

        @Test
        void noAtSign_treatedAsPlain() {
            // degenerate case — falls back to plain fitting
            String result = StringDataCreator.fitEmail("notanemail", 5);
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
            String result = StringDataCreator.fitUrl("http://example.com", 30);
            assertThat(result.length()).isGreaterThanOrEqualTo(18);
        }

        @Test
        void tooLong_trimmed() {
            String result = StringDataCreator.fitUrl("http://example.com/very/long/path", 20);
            assertThat(result).hasSize(20);
        }

        @Test
        void exact_unchanged() {
            String url = "http://example.com/x";
            assertThat(StringDataCreator.fitUrl(url, url.length())).isEqualTo(url);
        }
    }

    @Nested
    @DisplayName("StringShaper — fitPlain and fitToBounds remaining")
    class PlainAndBoundsFitting {

        // fitPlain: value.length() == target -> return value as-is (line 89)
        @Test
        void plainFitKeepsAnExactLength() {
            String v = StringDataCreator.fitPlain("hello", 5);
            assertThat(v).isEqualTo("hello");
        }

        // fitEmail: email.length() > target AND local.length() > newLocalLen (trim local)
        @Test
        void emailFitTrimsTheLocalPart() {
            // local = "abcdefgh" (8), domain = "@x.com" (6), total = 14, target = 8
            // newLocalLen = max(1, min(64, 8-6)) = 2 -> local trimmed to "ab"
            String result = StringDataCreator.fitEmail("abcdefgh@x.com", 8);
            assertThat(result).hasSize(8);
            assertThat(result).contains("@x.com");
        }

        // fitToBounds: default (NONE format) in max-exceeded switch
        @Test
        void defaultMaxTrimsTheValue() {
            // Plain string with max bound exceeded — hits default arm in max switch
            var fc = AnnotationResolver.analyzeFromStrings("desc", "String",
                    List.of("@Size(min=2, max=5)"));
            var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v.length()).isLessThanOrEqualTo(5);
        }

        // fitToBounds: default (NONE format) in min-not-met switch
        @Test
        void defaultMinPadsTheValue() {
            var fc = AnnotationResolver.analyzeFromStrings("code", "String",
                    List.of("@Size(min=20, max=50)"));
            var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v.length()).isGreaterThanOrEqualTo(20);
        }

        // fitToBounds URL max trim — hits URL arm in max-exceeded switch
        @Test
        void urlIsTrimmedToItsMaximum() {
            var fc = AnnotationResolver.analyzeFromStrings("site", "String",
                    List.of("@URL", "@Size(min=5, max=15)"));
            var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v.length()).isLessThanOrEqualTo(15);
        }

        // fitToBounds URL min pad — hits URL arm in min-not-met switch
        @Test
        void urlIsPaddedToItsMinimum() {
            var fc = AnnotationResolver.analyzeFromStrings("link", "String",
                    List.of("@URL", "@Size(min=40, max=200)"));
            var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(v.length()).isGreaterThanOrEqualTo(40);
        }
    }

    @Nested
    @DisplayName("StringShaper — fitEmail and fitUrl edge cases")
    class FitEmailAndUrlEdges {

        // fitEmail — no '@' in input → falls back to fitPlain
        @Test
        void emailWithoutAnAtSignIsFitted() {
            String result = StringDataCreator.fitEmail("noemail", 10);
            assertThat(result).hasSize(10);
        }

        // fitEmail — email.length() == target → returned as-is
        @Test
        void emailFitsAnExactLength() {
            String email = "a@b.com"; // 7 chars
            assertThat(StringDataCreator.fitEmail(email, 7)).isEqualTo(email);
        }

        // fitEmail — needed <= 0 after local fills gap → return local + domain without domain extension
        @Test
        void localPartAbsorbsAllTheExtraLength() {
            // local = "ab" (2 chars), domain = "@x.com" (6 chars), total = 8
            // target = 10, needed = 2, localRoom = 62, localGrowth = 2, needed becomes 0
            String email = "ab@x.com";
            String result = StringDataCreator.fitEmail(email, 10);
            assertThat(result).hasSize(10);
            assertThat(result).endsWith("@x.com");
        }

        // fitUrl — url.length() == target → returned as-is
        @Test
        void urlFitsAnExactLength() {
            String url = "http://x.com"; // 12 chars
            assertThat(StringDataCreator.fitUrl(url, 12)).isEqualTo(url);
        }

        // fitUrl — url longer than target → trimmed
        @Test
        void urlFitTrimsToTheLength() {
            String url = "http://example.com/path";
            String result = StringDataCreator.fitUrl(url, 10);
            assertThat(result).hasSize(10);
        }

        // fitUrl — url shorter than target → padded
        @Test
        void urlFitPadsToTheLength() {
            String url = "http://x.io";
            String result = StringDataCreator.fitUrl(url, 20);
            assertThat(result).hasSize(20);
        }
    }
}
