package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers remaining StringShaper branches:
 * baseValue  — ISBN, EAN format arms
 * fitEmail   — domainBody extension when local is full (needed > 0 after localGrowth)
 * fitToBounds — URL trim (max exceeded), URL pad (min not met)
 */
@DisplayName("StringShaper — edge case branches")
class StringShaperEdgeCaseTest {

    @Nested
    @DisplayName("baseValue — ISBN and EAN format arms")
    class BaseValueFormats {

        @Test
        void isbn_format() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("isbn", "String", List.of("@ISBN"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isNotBlank();
        }

        @Test
        void ean_format() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("ean", "String", List.of("@EAN"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v).isNotBlank();
        }
    }

    @Nested
    @DisplayName("fitEmail — domainBody extension branch")
    class FitEmailDomainExtension {

        @Test
        void local_full_at_64_extends_domain() {
            // Email where local is exactly 64 chars — padding must extend domain
            String longLocal = "a".repeat(64);
            String email = longLocal + "@example.com";
            // target > email.length() AND local is already at max 64 -> needed > 0 after localGrowth
            String result = StringShaper.fitEmail(email, email.length() + 5);
            assertThat(result).hasSize(email.length() + 5);
            assertThat(result).contains("@");
        }

        @Test
        void needed_zero_after_local_growth_no_domain_extension() {
            // local has room to grow, all needed filled by local — needed becomes 0
            String email = "ab@example.com"; // local="ab", domain="@example.com"
            // target = email.length() + 2 — local can grow 2, needed becomes 0
            String result = StringShaper.fitEmail(email, email.length() + 2);
            assertThat(result).hasSize(email.length() + 2);
            assertThat(result).endsWith("@example.com");
        }
    }

    @Nested
    @DisplayName("fitToBounds — URL trim and pad")
    class FitToBoundsUrl {

        @Test
        void url_exceeds_max_trimmed() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("site", "String",
                    List.of("@URL", "@Size(min=5, max=20)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isLessThanOrEqualTo(20);
        }

        @Test
        void url_below_min_padded() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("site", "String",
                    List.of("@URL", "@Size(min=50, max=200)"));
            var v = (String) StringShaper.shape(fc, BoundaryTarget.semantic());
            assertThat(v.length()).isGreaterThanOrEqualTo(50);
        }
    }
}
