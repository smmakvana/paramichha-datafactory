package com.paramichha.datafactory.constraint;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** The convenience methods on FieldConstraints. */
@DisplayName("FieldConstraints")
class FieldConstraintsTest {

    @Nested
    @DisplayName("FieldConstraints convenience methods")
    class ConvenienceMethods {

        private FieldConstraints make(String ann) {
            return AnnotationResolver.analyzeFromStrings("f", "LocalDate", List.of(ann));
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
            assertThat(AnnotationResolver.analyzeFromStrings("e", "String", List.of("@Email")).hasFormat()).isTrue();
        }

        @Test
        void hasFormat_false() {
            assertThat(AnnotationResolver.analyzeFromStrings("n", "String", List.of()).hasFormat()).isFalse();
        }

        @Test
        void hasBounds_true() {
            assertThat(AnnotationResolver.analyzeFromStrings("n", "Integer", List.of("@Min(1)")).hasBounds()).isTrue();
        }

        @Test
        void hasBounds_false() {
            assertThat(AnnotationResolver.analyzeFromStrings("n", "Integer", List.of()).hasBounds()).isFalse();
        }

        @Test
        void hasTemporal_true() {
            assertThat(make("@Past").hasTemporal()).isTrue();
        }

        @Test
        void hasTemporal_false() {
            assertThat(AnnotationResolver.analyzeFromStrings("n", "String", List.of()).hasTemporal()).isFalse();
        }
    }
}
