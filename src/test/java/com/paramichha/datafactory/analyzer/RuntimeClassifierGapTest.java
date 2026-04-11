package com.paramichha.datafactory.analyzer;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Targets uncovered branches in RuntimeAnnotationClassifier and AnnotationAnalyzer:
 * FormatType — ISBN, EAN, UUID_STRING
 * QuantityBounds — DecimalMin, DecimalMax, Size/Length zero-min/maxInt trim
 * AnnotationAnalyzer — contradictory bounds warning, char/Character resolveType
 */
@DisplayName("RuntimeAnnotationClassifier + AnnotationAnalyzer — coverage gaps")
class RuntimeClassifierGapTest {

    private static FieldConstraints analyze(String name, Class<?> type, Annotation... annotations) {
        return AnnotationAnalyzer.analyze(name, type, List.of(annotations));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FormatType — ISBN, EAN, UUID_STRING
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RuntimeAnnotationClassifier — format types")
    class FormatTypes {

        @Test
        void isbn() throws Exception {
            var ann = (ISBN) IsbnHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("isbn", String.class, ann);
            assertThat(fc.format()).isEqualTo(FormatType.ISBN);
        }

        @Test
        void ean() throws Exception {
            var ann = (EAN) EanHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("ean", String.class, ann);
            assertThat(fc.format()).isEqualTo(FormatType.EAN);
        }

        @Test
        void uuid_string() throws Exception {
            var ann = (UUID) UuidHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("uid", String.class, ann);
            assertThat(fc.format()).isEqualTo(FormatType.UUID_STRING);
        }

        // Annotation holder classes
        static class IsbnHolder {
            @ISBN
            String f;
        }

        static class EanHolder {
            @EAN
            String f;
        }

        static class UuidHolder {
            @UUID
            String f;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DecimalMin / DecimalMax
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RuntimeAnnotationClassifier — DecimalMin / DecimalMax")
    class DecimalBounds {

        @Test
        void decimalMin_sets_lower_bound() throws Exception {
            var ann = (DecimalMin) DecimalMinHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("price", java.math.BigDecimal.class, ann);
            assertThat(fc.bounds().hasMin()).isTrue();
            assertThat(fc.bounds().min()).isEqualTo(10L);
        }

        @Test
        void decimalMax_sets_upper_bound() throws Exception {
            var ann = (DecimalMax) DecimalMaxHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("price", java.math.BigDecimal.class, ann);
            assertThat(fc.bounds().hasMax()).isTrue();
            assertThat(fc.bounds().max()).isEqualTo(999L);
        }

        static class DecimalMinHolder {
            @DecimalMin("10")
            java.math.BigDecimal f;
        }

        static class DecimalMaxHolder {
            @DecimalMax("999")
            java.math.BigDecimal f;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Size — zero min trimmed, Integer.MAX_VALUE max trimmed
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RuntimeAnnotationClassifier — Size bounds trimming")
    class SizeBoundsTrimming {

        @Test
        void size_zero_min_omitted() throws Exception {
            var ann = (Size) SizeZeroMinHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("name", String.class, ann);
            assertThat(fc.bounds().hasMin()).isFalse();
            assertThat(fc.bounds().max()).isEqualTo(50L);
        }

        @Test
        void size_maxint_max_omitted() throws Exception {
            var ann = (Size) SizeMaxIntHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("name", String.class, ann);
            assertThat(fc.bounds().hasMax()).isFalse();
            assertThat(fc.bounds().min()).isEqualTo(2L);
        }

        @Test
        void length_zero_min_omitted() throws Exception {
            var ann = (Length) LengthZeroMinHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("name", String.class, ann);
            assertThat(fc.bounds().hasMin()).isFalse();
            assertThat(fc.bounds().max()).isEqualTo(30L);
        }

        @Test
        void length_maxint_max_omitted() throws Exception {
            var ann = (Length) LengthMaxIntHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("name", String.class, ann);
            assertThat(fc.bounds().hasMax()).isFalse();
            assertThat(fc.bounds().min()).isEqualTo(3L);
        }

        static class SizeZeroMinHolder {
            @Size(min = 0, max = 50)
            String f;
        }

        static class SizeMaxIntHolder {
            @Size(min = 2, max = Integer.MAX_VALUE)
            String f;
        }

        static class LengthZeroMinHolder {
            @Length(min = 0, max = 30)
            String f;
        }

        static class LengthMaxIntHolder {
            @Length(min = 3, max = Integer.MAX_VALUE)
            String f;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Contradictory bounds — warning added
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AnnotationAnalyzer — contradictory bounds warning")
    class ContradictoryBounds {

        @Test
        void min_greater_than_max_adds_warning() throws Exception {
            var minAnn = (Min) MinHolder.class.getDeclaredField("f").getAnnotations()[0];
            var maxAnn = (Max) MaxHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = analyze("n", Integer.class, maxAnn, minAnn); // max=5, min=10 -> contradictory
            assertThat(fc.hasWarnings()).isTrue();
            assertThat(fc.warnings().get(0)).contains("Min(10) > Max(5)");
        }

        static class MinHolder {
            @Min(10)
            int f;
        }

        static class MaxHolder {
            @Max(5)
            int f;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // resolveType — char / Character
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AnnotationAnalyzer.resolveType — char / Character")
    class CharResolveType {

        @Test
        void char_primitive_resolves_to_Character() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("c", "char", List.of());
            assertThat(fc.fieldType()).isEqualTo(Character.class);
        }

        @Test
        void Character_wrapper_resolves_to_Character() {
            var fc = AnnotationAnalyzer.analyzeFromStrings("c", "Character", List.of());
            assertThat(fc.fieldType()).isEqualTo(Character.class);
        }
    }
}
