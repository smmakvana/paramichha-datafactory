package com.paramichha.datafactory.constraint;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Classifying annotation instances: format types and bounds. */
@DisplayName("RuntimeAnnotationClassifier")
class RuntimeAnnotationClassifierTest {

    @Nested
    @DisplayName("RuntimeAnnotationClassifier — all format types")
    class FormatTypes {

        @Test
        void creditCardNumber() {
            var f = AnnotationResolver.analyzeFromStrings("cc", "String", List.of("@CreditCardNumber"));
            assertThat(f.format()).isEqualTo(FormatType.CREDIT_CARD);
        }

        @Test
        void isbn() {
            var f = AnnotationResolver.analyzeFromStrings("isbn", "String", List.of("@ISBN"));
            assertThat(f.format()).isEqualTo(FormatType.ISBN);
        }

        @Test
        void ean() {
            var f = AnnotationResolver.analyzeFromStrings("ean", "String", List.of("@EAN"));
            assertThat(f.format()).isEqualTo(FormatType.EAN);
        }

        // UUID_STRING — Hibernate @UUID annotation — only available via runtime classifier
        // Skip here as it requires hibernate-specific annotation instance
    }

    @Nested
    @DisplayName("RuntimeAnnotationClassifier — additional gaps")
    class AdditionalFormatsAndBounds {

        @Test
        void uuidStringFormat() throws Exception {
            var ann = (org.hibernate.validator.constraints.UUID)
                    UuidHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = AnnotationResolver.analyze("uid", String.class, java.util.List.of(ann));
            assertThat(fc.format()).isEqualTo(FormatType.UUID_STRING);
        }

        @Test
        void decimalMax_sets_upper_bound() throws Exception {
            var ann = (jakarta.validation.constraints.DecimalMax)
                    DecimalMaxHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = AnnotationResolver.analyze("price", java.math.BigDecimal.class, java.util.List.of(ann));
            assertThat(fc.bounds() instanceof NumericBounds.DecimalBounds).isTrue();
            assertThat(((NumericBounds.DecimalBounds) fc.bounds()).decMax()).isEqualByComparingTo("999");
        }

        @Test
        void lengthZeroMinOmitted() throws Exception {
            var ann = (org.hibernate.validator.constraints.Length)
                    LengthZeroMinHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = AnnotationResolver.analyze("name", String.class, java.util.List.of(ann));
            assertThat(fc.bounds().hasMin()).isFalse();
            assertThat(fc.bounds().max()).isEqualTo(30L);
        }

        @Test
        void lengthMaxintMaxOmitted() throws Exception {
            var ann = (org.hibernate.validator.constraints.Length)
                    LengthMaxIntHolder.class.getDeclaredField("f").getAnnotations()[0];
            var fc = AnnotationResolver.analyze("name", String.class, java.util.List.of(ann));
            assertThat(fc.bounds().hasMax()).isFalse();
            assertThat(fc.bounds().min()).isEqualTo(3L);
        }

        @Test
        void charPrimitiveResolvesToCharacter() {
            var fc = AnnotationResolver.analyzeFromStrings("c", "char", java.util.List.of());
            assertThat(fc.fieldType()).isEqualTo(Character.class);
        }

        @Test
        void Character_wrapper_resolves_to_Character() {
            var fc = AnnotationResolver.analyzeFromStrings("c", "Character", java.util.List.of());
            assertThat(fc.fieldType()).isEqualTo(Character.class);
        }

        // annotation holder inner classes
        static class UuidHolder {
            @org.hibernate.validator.constraints.UUID
            String f;
        }
        static class DecimalMaxHolder {
            @jakarta.validation.constraints.DecimalMax("999")
            java.math.BigDecimal f;
        }
        static class LengthZeroMinHolder {
            @org.hibernate.validator.constraints.Length(min = 0, max = 30)
            String f;
        }
        static class LengthMaxIntHolder {
            @org.hibernate.validator.constraints.Length(min = 3, max = Integer.MAX_VALUE)
            String f;
        }
        static class MinHolder {
            @jakarta.validation.constraints.Min(10)
            int f;
        }
        static class MaxHolder {
            @jakarta.validation.constraints.Max(5)
            int f;
        }
    }
}
