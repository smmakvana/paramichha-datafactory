package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static com.paramichha.datafactory.generation.ShaperTestSupport.*;
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

    @Nested
    @DisplayName("StringTypeShaper")
    class TypeShaperDelegation {
        @Test
        void supportsString() {
            assertThat(StringDataCreator.INSTANCE.supports(String.class)).isTrue();
        }

        @Test
        void supportsIntFalse() {
            assertThat(StringDataCreator.INSTANCE.supports(int.class)).isFalse();
        }

        @Test
        void shapeDelegates() {
            var f = field("name", String.class, "@NotBlank");
            assertThat(StringDataCreator.INSTANCE.shape(f, semantic(), DataMachines.random())).isInstanceOf(String.class);
        }
    }

    @Nested
    @DisplayName("StringShaper.shape() — format branches")
    class FormatBranches {

        @Test
        void emailSemanticFitsBounds() {
            // @Email with @Size bounds — semantic path calls fitToBounds
            var f = field("email", String.class, "@Email", "@NotBlank", "@Size(min=8, max=100)");
            String v = StringDataCreator.getString(f, semantic(), DataMachines.random());
            assertThat(v).contains("@");
        }

        @Test
        void urlSemantic() {
            var f = field("site", String.class, "@URL");
            String v = StringDataCreator.getString(f, semantic(), DataMachines.random());
            assertThat(v).matches("https?://\\S+");
        }

        @Test
        void urlWithTarget() {
            var f = field("site", String.class, "@URL");
            String v = StringDataCreator.getString(f, atMin(30), DataMachines.random());
            assertThat(v).hasSizeGreaterThanOrEqualTo(20);
        }

        @Test
        void patternSemantic() {
            // @Pattern — best-effort, returns base value
            var f = field("code", String.class, "@Pattern(regexp=\"^[A-Z]+$\")");
            String v = StringDataCreator.getString(f, semantic(), DataMachines.random());
            assertThat(v).matches("[A-Z]+");
        }

        @Test
        void plainWithTarget() {
            var f = field("name", String.class, "@NotBlank", "@Size(min=2, max=50)");
            String v = StringDataCreator.getString(f, atMin(5), DataMachines.random());
            assertThat(v).hasSize(5);
        }

        @Test
        void semanticByName_phone() {
            var f = fieldOf("phone", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\(\\d{3}\\) \\d{3}-\\d{4}");
        }

        @Test
        void semanticByName_mobile() {
            var f = fieldOf("mobile", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\(\\d{3}\\) \\d{3}-\\d{4}");
        }

        @Test
        void semanticByName_firstName() {
            var f = fieldOf("firstName", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\p{Lu}\\p{Ll}+");
        }

        @Test
        void semanticByName_lastName() {
            var f = fieldOf("lastName", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\p{Lu}\\p{Ll}+");
        }

        @Test
        void semanticByName_name() {
            var f = fieldOf("name", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("([A-Z][a-z]+\\. )?[A-Z][a-z]+( [A-Z][\\w.]*)+");
        }

        @Test
        void semanticByName_password() {
            var f = fieldOf("password", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\S{8,}");
        }

        @Test
        void semanticByName_url() {
            var f = fieldOf("url", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("https?://\\S+");
        }

        @Test
        void semanticByName_city() {
            var f = fieldOf("city", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\p{Lu}[\\p{L} ().,'-]+");
        }

        @Test
        void semanticByName_countryCode() {
            var f = fieldOf("countryCode", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[A-Z]{2}");
        }

        @Test
        void semanticByName_country() {
            var f = fieldOf("country", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\p{Lu}[\\p{L} ().,'-]+");
        }

        @Test
        void semanticByName_address() {
            var f = fieldOf("address", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\d+ [A-Z].*");
        }

        @Test
        void semanticByName_postcode() {
            var f = fieldOf("postcode", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\d{5}");
        }

        @Test
        void semanticByName_zip() {
            var f = fieldOf("zip", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\d{5}");
        }

        @Test
        void semanticByName_currency() {
            var f = fieldOf("currency", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[A-Z]{3}");
        }

        @Test
        void semanticByName_description() {
            var f = fieldOf("description", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[A-Z].*\\.");
        }

        @Test
        void semanticByName_title() {
            var f = fieldOf("title", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\p{Lu}[\\p{L} ().,'-]+");
        }

        @Test
        void semanticByName_company() {
            var f = fieldOf("company", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[A-Z].+");
        }

        @Test
        void semanticByName_token() {
            var f = fieldOf("token", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        void semanticByName_reference() {
            var f = fieldOf("reference", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("REF-\\d{6}");
        }

        @Test
        void semanticByName_id() {
            var f = fieldOf("userId", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        void semanticByName_code() {
            var f = fieldOf("productCode", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("\\d{9}[\\dX]");
        }

        @Test
        void semanticByName_status() {
            var f = fieldOf("status", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[A-Z_]+");
        }

        @Test
        void semanticByName_type() {
            var f = fieldOf("type", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[A-Z_]+");
        }

        @Test
        void semanticByName_message() {
            var f = fieldOf("message", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[A-Z].*\\.");
        }

        @Test
        void semanticByName_fallback_word() {
            var f = fieldOf("zzz", String.class);
            assertThat(StringDataCreator.getString(f, semantic(), DataMachines.random())).matches("[a-z]+");
        }

        @Test
        void fitToBounds_tooLong_email() {
            // semantic email longer than @Size max → fitToBounds trims via fitEmail
            var f = field("email", String.class, "@Email", "@Size(min=8, max=20)");
            String v = StringDataCreator.getString(f, semantic(), DataMachines.random());
            assertThat(v.length()).isLessThanOrEqualTo(20);
        }

        @Test
        void fitToBounds_tooShort_email() {
            // semantic email shorter than @Size min → fitToBounds pads via fitEmail
            var f = field("email", String.class, "@Email", "@Size(min=30, max=80)");
            String v = StringDataCreator.getString(f, semantic(), DataMachines.random());
            assertThat(v.length()).isGreaterThanOrEqualTo(30);
        }

        @Test
        void fitToBounds_url_tooLong() {
            var f = field("site", String.class, "@URL", "@Size(min=5, max=15)");
            String v = StringDataCreator.getString(f, semantic(), DataMachines.random());
            assertThat(v.length()).isLessThanOrEqualTo(15);
        }
    }

    @Nested
    @DisplayName("StringShaper.shape() — additional hint branches")
    class AdditionalHints {

        @Test
        void linkHint() {
            assertThat(StringDataCreator.getString(fieldOf("link", String.class), semantic(), DataMachines.random())).matches("https?://\\S+");
        }

        @Test
        void websiteHint() {
            assertThat(StringDataCreator.getString(fieldOf("website", String.class), semantic(), DataMachines.random())).matches("https?://\\S+");
        }

        @Test
        void postalHint() {
            assertThat(StringDataCreator.getString(fieldOf("postalCode", String.class), semantic(), DataMachines.random())).matches("\\d{5}");
        }

        @Test
        void ccyHint() {
            assertThat(StringDataCreator.getString(fieldOf("ccy", String.class), semantic(), DataMachines.random())).matches("[A-Z]{3}");
        }

        @Test
        void noteHint() {
            assertThat(StringDataCreator.getString(fieldOf("note", String.class), semantic(), DataMachines.random())).matches("[A-Z].*\\.");
        }

        @Test
        void commentHint() {
            assertThat(StringDataCreator.getString(fieldOf("comment", String.class), semantic(), DataMachines.random())).matches("[A-Z].*\\.");
        }

        @Test
        void organisationHint() {
            assertThat(StringDataCreator.getString(fieldOf("organisation", String.class), semantic(), DataMachines.random())).matches("[A-Z].+");
        }

        @Test
        void refHint() {
            assertThat(StringDataCreator.getString(fieldOf("ref", String.class), semantic(), DataMachines.random())).matches("REF-\\d{6}");
        }

        @Test
        void reasonHint() {
            assertThat(StringDataCreator.getString(fieldOf("reason", String.class), semantic(), DataMachines.random())).matches("[A-Z].*\\.");
        }
    }
}
