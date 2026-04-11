package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.builder.ConstraintCase;
import com.paramichha.datafactory.builder.DefaultFieldBuilder;
import com.paramichha.datafactory.builder.FieldBuilderFactory;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FieldBuilder")
class FieldBuilderTest {

    // ── helper — returns DefaultFieldBuilder so constraintCases() is accessible ──
    @SuppressWarnings("unchecked")
    private static <T> DefaultFieldBuilder<T> dfb(Class<T> type) {
        return (DefaultFieldBuilder<T>) FieldBuilderFactory.create(type);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALID VALUES — annotated fields
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validAll() — annotated fields")
    class ValidValuesAnnotated {

        @Test
        void email_allValuesContainAt() {
            var fb = FieldBuilderFactory.create(String.class).with(NotBlank.class).with(Email.class);
            assertThat(fb.validList()).isNotEmpty();
            fb.validList().forEach(v -> assertThat(v).contains("@"));
        }

        @Test
        void stringWithSize_allValuesFitBounds() {
            var fb = FieldBuilderFactory.create(String.class)
                                        .with(NotBlank.class)
                                        .with("@Size(min=5, max=20)");
            fb.validList().forEach(v -> assertThat(v.length()).isBetween(5, 20));
        }

        @Test
        void emailWithSize_allValuesAreEmailAndFitBounds() {
            var fb = FieldBuilderFactory.create(String.class)
                                        .with(NotBlank.class).with(Email.class)
                                        .with("@Size(min=8, max=50)");
            fb.validList().forEach(v -> {
                assertThat(v).contains("@");
                assertThat(v.length()).isBetween(8, 50);
            });
        }

        @Test
        void integerWithMinMax_allWithinBoundsAndIncludesBoundaries() {
            var fb = FieldBuilderFactory.create(Integer.class).with("@Min(18)").with("@Max(120)");
            List<Integer> values = fb.validList();
            assertThat(values).hasSizeGreaterThanOrEqualTo(3);
            values.forEach(v -> assertThat(v).isBetween(18, 120));
            assertThat(values).contains(18, 120);
        }

        @Test
        void booleanAssertTrue_onlyTrue() {
            var fb = FieldBuilderFactory.create(Boolean.class).with(AssertTrue.class);
            assertThat(fb.validList()).containsExactly(true);
        }

        @Test
        void localDatePast_valueIsBeforeToday() {
            var fb = FieldBuilderFactory.create(LocalDate.class).with(Past.class);
            fb.validList().forEach(v -> assertThat(v).isBefore(LocalDate.now()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALID VALUES — unannotated fields
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validAll() — unannotated fields")
    class ValidValuesUnannotated {

        @Test
        void string_noAnnotations_returnsNonBlankValue() {
            var fb = FieldBuilderFactory.create(String.class);
            assertThat(fb.validList()).isNotEmpty();
            fb.validList().forEach(v -> assertThat(v).isNotBlank());
        }

        @Test
        void integer_noAnnotations_returnsTypeDefaultRange() {
            var values = FieldBuilderFactory.create(Integer.class).validList();
            assertThat(values).isNotEmpty();
            // unannotated int → type-natural bounds: MIN_VALUE, MIN_VALUE+1, midpoint, MAX_VALUE-1, MAX_VALUE + semantic
            assertThat(values).contains(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
        }

        @Test
        void boolean_noAnnotations_returnsTrue() {
            assertThat(FieldBuilderFactory.create(Boolean.class).validList()).contains(true);
        }

        @Test
        void long_noAnnotations_returnsTypeDefaultRange() {
            var values = FieldBuilderFactory.create(Long.class).validList();
            assertThat(values).isNotEmpty();
            // unannotated long → type-natural bounds: Long.MIN_VALUE to Long.MAX_VALUE
            assertThat(values).contains(Long.MIN_VALUE, 0L, Long.MAX_VALUE);
        }

        @Test
        void localDate_noAnnotations_returnsNonNull() {
            var fb = FieldBuilderFactory.create(LocalDate.class);
            assertThat(fb.validList()).isNotEmpty();
            fb.validList().forEach(v -> assertThat(v).isNotNull());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTRAINT CASES — annotated fields
    // (cast to DefaultFieldBuilder — constraintCases() is engine-internal)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("constraintCases() — annotated fields")
    class ConstraintCasesAnnotated {

        @Test
        void notNull_producesNullCase() {
            assertThat(dfb(String.class).with(NotNull.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("NotNull") && c.value() == null);
        }

        @Test
        void notBlank_producesNullAndBlankCases() {
            var cases = dfb(String.class).with(NotBlank.class).constraintCases();
            assertThat(cases).anyMatch(c -> c.value() == null);
            assertThat(cases).anyMatch(c -> "".equals(c.value()));
        }

        @Test
        void email_producesInvalidFormatCase() {
            assertThat(dfb(String.class).with(NotBlank.class).with(Email.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("Email")
                            && !c.value().toString().contains("@"));
        }

        @Test
        void minConstraint_producesBelowMinCase() {
            assertThat(dfb(Integer.class).with("@Min(18)").constraintCases())
                    .anyMatch(c -> c.constraint().equals("Min") && (Integer) c.value() < 18);
        }

        @Test
        void maxConstraint_producesAboveMaxCase() {
            assertThat(dfb(Integer.class).with("@Max(120)").constraintCases())
                    .anyMatch(c -> c.constraint().equals("Max") && (Integer) c.value() > 120);
        }

        @Test
        void sizeConstraint_producesTooShortAndTooLong() {
            var cases = dfb(String.class).with(NotBlank.class).with("@Size(min=5, max=20)").constraintCases();
            assertThat(cases).anyMatch(c -> c.constraint().equals("Size")
                    && c.value() != null && c.value().toString().length() < 5);
            assertThat(cases).anyMatch(c -> c.constraint().equals("Size")
                    && c.value() != null && c.value().toString().length() > 20);
        }

        @Test
        void negativeConstraint_producesPositiveCase() {
            assertThat(dfb(Integer.class).with("@Max(-1)").constraintCases())
                    .anyMatch(c -> c.constraint().equals("Max") && (Integer) c.value() == 0);
        }

        @Test
        void assertTrue_producesFalseCase() {
            assertThat(dfb(Boolean.class).with(AssertTrue.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("AssertTrue")
                            && Boolean.FALSE.equals(c.value()));
        }

        @Test
        void past_producesNotPastCase() {
            assertThat(dfb(LocalDate.class).with(Past.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("Past")
                            && ((LocalDate) c.value()).isAfter(LocalDate.now()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTRAINT CASES — unannotated fields
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("constraintCases() — unannotated fields")
    class ConstraintCasesUnannotated {

        @Test
        void string_nullIsTypeDefault() {
            assertThat(dfb(String.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("Null") && c.value() == null);
        }

        @Test
        void string_blankIsTypeDefault() {
            assertThat(dfb(String.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("Blank") && "".equals(c.value()));
        }

        @Test
        void integer_negativeIsTypeDefault() {
            assertThat(dfb(Integer.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("Negative") && (Integer) c.value() < 0);
        }

        @Test
        void long_negativeIsTypeDefault() {
            assertThat(dfb(Long.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("Negative") && (Long) c.value() < 0);
        }

        @Test
        void boolean_falseIsTypeDefault() {
            assertThat(dfb(Boolean.class).constraintCases())
                    .anyMatch(c -> c.constraint().equals("False")
                            && Boolean.FALSE.equals(c.value()));
        }

        @Test
        void localDate_nullIsTypeDefault() {
            assertThat(dfb(LocalDate.class).constraintCases())
                    .anyMatch(c -> c.value() == null);
        }

        @Test
        void integer_hasAtLeastOneInvalidCase() {
            assertThat(dfb(Integer.class).constraintCases()).isNotEmpty();
        }

        @Test
        void string_hasAtLeastTwoInvalidCases() {
            assertThat(dfb(String.class).constraintCases())
                    .hasSizeGreaterThanOrEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NO DUPLICATES
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("No duplicate constraint cases")
    class NoDuplicates {

        @Test
        void notNull_exactlyOneNullCase() {
            long count = dfb(String.class).with(NotNull.class)
                    .constraintCases().stream().filter(c -> c.value() == null).count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        void notBlank_exactlyOneBlankCase() {
            long count = dfb(String.class).with(NotBlank.class)
                    .constraintCases().stream().filter(c -> "".equals(c.value())).count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        void constraintKeys_areUnique() {
            List<String> keys = dfb(String.class)
                    .with(NotBlank.class).with(Email.class).with("@Size(min=8, max=50)")
                    .constraintCases().stream()
                    .map(c -> c.constraint() + ":" + c.value()).toList();
            assertThat(keys).doesNotHaveDuplicates();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // isAnnotationDriven flag
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isAnnotationDriven flag")
    class AnnotationDrivenFlag {

        @Test
        void unannotatedNull_isNotAnnotationDriven() {
            assertThat(dfb(String.class).constraintCases())
                    .anyMatch(c -> c.value() == null && !c.annotationDriven());
        }

        @Test
        void unannotatedBlank_isNotAnnotationDriven() {
            assertThat(dfb(String.class).constraintCases())
                    .anyMatch(c -> "".equals(c.value()) && !c.annotationDriven());
        }

        @Test
        void unannotatedNegative_isNotAnnotationDriven() {
            assertThat(dfb(Integer.class).constraintCases())
                    .anyMatch(c -> !c.annotationDriven()
                            && c.value() instanceof Integer i && i < 0);
        }

        @Test
        void annotatedNull_isAnnotationDriven() {
            assertThat(dfb(String.class).with(NotNull.class).constraintCases())
                    .anyMatch(c -> c.value() == null && c.annotationDriven());
        }

        @Test
        void annotationCases_onlyAnnotationDriven() {
            assertThat(dfb(String.class).with(NotBlank.class).with(Email.class).annotationCases())
                    .allMatch(ConstraintCase::annotationDriven);
        }

        @Test
        void annotatedEmail_allCasesAreAnnotationDriven() {
            assertThat(dfb(String.class)
                    .with(NotBlank.class).with(Email.class).with("@Size(min=8, max=50)")
                    .constraintCases())
                    .allMatch(ConstraintCase::annotationDriven);
        }
    }
}
