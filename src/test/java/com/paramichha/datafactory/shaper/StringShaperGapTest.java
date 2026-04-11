package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers remaining gaps in StringShaper — semanticByName hint branches
 * not yet exercised by any other test.
 */
@DisplayName("StringShaper — semanticByName gaps")
class StringShaperGapTest {

    private static String shape(String name) {
        var fc = AnnotationAnalyzer.analyzeFromStrings(name, "String", List.of());
        return (String) StringShaper.shape(fc, BoundaryTarget.semantic());
    }

    @Nested
    @DisplayName("semanticByName — all hint branches")
    class SemanticByName {

        @Test
        void email_hint() {
            assertThat(shape("email")).contains("@");
        }

        @Test
        void phone_hint() {
            assertThat(shape("phone")).isNotBlank();
        }

        @Test
        void mobile_hint() {
            assertThat(shape("mobileNumber")).isNotBlank();
        }

        @Test
        void firstName_hint() {
            assertThat(shape("firstName")).isNotBlank();
        }

        @Test
        void first_name_hint() {
            assertThat(shape("first_name")).isNotBlank();
        }

        @Test
        void lastName_hint() {
            assertThat(shape("lastName")).isNotBlank();
        }

        @Test
        void last_name_hint() {
            assertThat(shape("last_name")).isNotBlank();
        }

        @Test
        void name_hint() {
            assertThat(shape("name")).isNotBlank();
        }

        @Test
        void password_hint() {
            assertThat(shape("password")).isNotBlank();
        }

        @Test
        void url_hint() {
            assertThat(shape("url")).isNotBlank();
        }

        @Test
        void link_hint() {
            assertThat(shape("link")).isNotBlank();
        }

        @Test
        void website_hint() {
            assertThat(shape("website")).isNotBlank();
        }

        @Test
        void city_hint() {
            assertThat(shape("city")).isNotBlank();
        }

        @Test
        void countryCode_hint() {
            assertThat(shape("countryCode")).isNotBlank();
        }

        @Test
        void country_hint() {
            assertThat(shape("country")).isNotBlank();
        }

        @Test
        void address_hint() {
            assertThat(shape("address")).isNotBlank();
        }

        @Test
        void postcode_hint() {
            assertThat(shape("postcode")).isNotBlank();
        }

        @Test
        void zip_hint() {
            assertThat(shape("zipCode")).isNotBlank();
        }

        @Test
        void postal_hint() {
            assertThat(shape("postalCode")).isNotBlank();
        }

        @Test
        void currency_hint() {
            assertThat(shape("currency")).isNotBlank();
        }

        @Test
        void ccy_hint() {
            assertThat(shape("ccy")).isNotBlank();
        }

        @Test
        void description_hint() {
            assertThat(shape("description")).isNotBlank();
        }

        @Test
        void note_hint() {
            assertThat(shape("note")).isNotBlank();
        }

        @Test
        void comment_hint() {
            assertThat(shape("comment")).isNotBlank();
        }

        @Test
        void title_hint() {
            assertThat(shape("title")).isNotBlank();
        }

        @Test
        void company_hint() {
            assertThat(shape("company")).isNotBlank();
        }

        @Test
        void organisation_hint() {
            assertThat(shape("organisation")).isNotBlank();
        }

        @Test
        void token_hint() {
            assertThat(shape("token")).isNotBlank();
        }

        @Test
        void reference_hint() {
            assertThat(shape("reference")).isNotBlank();
        }

        @Test
        void ref_hint() {
            assertThat(shape("ref")).isNotBlank();
        }

        @Test
        void userId_hint() {
            assertThat(shape("userId")).isNotBlank();
        }

        @Test
        void code_hint() {
            assertThat(shape("productCode")).isNotBlank();
        }

        @Test
        void status_hint() {
            assertThat(shape("status")).isEqualTo("ACTIVE");
        }

        @Test
        void type_hint() {
            assertThat(shape("type")).isEqualTo("DEFAULT");
        }

        @Test
        void message_hint() {
            assertThat(shape("message")).isNotBlank();
        }

        @Test
        void reason_hint() {
            assertThat(shape("reason")).isNotBlank();
        }

        @Test
        void fallback_word() {
            assertThat(shape("zzz")).isNotBlank();
        }
    }

    @Nested
    @DisplayName("fitToBounds — URL and EMAIL with bounds")
    class FitToBounds {

        @Test
        void email_tooLong_trimmed() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("email", "String",
                    List.of("@Email", "@Size(min=5, max=20)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isLessThanOrEqualTo(20);
        }

        @Test
        void email_tooShort_padded() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("email", "String",
                    List.of("@Email", "@Size(min=30, max=80)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isGreaterThanOrEqualTo(30);
        }

        @Test
        void url_tooLong_trimmed() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("site", "String",
                    List.of("@URL", "@Size(min=5, max=15)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isLessThanOrEqualTo(15);
        }

        @Test
        void pattern_returns_base_value() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("code", "String",
                    List.of("@Pattern(regexp=\"^[A-Z]+$\")"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isNotNull();
        }
    }
}
