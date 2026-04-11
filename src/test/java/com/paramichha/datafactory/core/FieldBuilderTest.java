package com.paramichha.datafactory.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FieldBuilder")
class FieldBuilderTest {

    // ═══════════════════════════════════════════════════════════════════════
    // VALID VALUES — annotated fields
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validValues() — annotated fields")
    class ValidValuesAnnotated {

        @Test
        void email_allValuesContainAt() {
            var ftv = FieldBuilder.of("email", "String",
                    List.of("@NotBlank", "@Email"));
            assertThat(ftv.validValues()).isNotEmpty();
            ftv.validValues().forEach(v -> assertThat(v.toString()).contains("@"));
        }

        @Test
        void stringWithSize_allValuesFitBounds() {
            var ftv = FieldBuilder.of("name", "String",
                    List.of("@NotBlank", "@Size(min=5, max=20)"));
            ftv.validValues().forEach(v ->
                    assertThat(v.toString().length()).isBetween(5, 20));
        }

        @Test
        void emailWithSize_allValuesAreEmailAndFitBounds() {
            var ftv = FieldBuilder.of("email", "String",
                    List.of("@NotBlank", "@Email", "@Size(min=8, max=50)"));
            ftv.validValues().forEach(v -> {
                assertThat(v.toString()).contains("@");
                assertThat(v.toString().length()).isBetween(8, 50);
            });
        }

        @Test
        void integerWithMinMax_allWithinBoundsAndIncludesBoundaries() {
            var ftv = FieldBuilder.of("age", "Integer",
                    List.of("@Min(18)", "@Max(120)"));
            List<Object> values = ftv.validValues();
            assertThat(values).hasSizeGreaterThanOrEqualTo(3);
            values.forEach(v -> assertThat((Integer) v).isBetween(18, 120));
            assertThat(values).contains(18, 120);
        }

        @Test
        void booleanAssertTrue_onlyTrue() {
            var ftv = FieldBuilder.of("active", "Boolean",
                    List.of("@AssertTrue"));
            assertThat(ftv.validValues()).containsExactly(true);
        }

        @Test
        void localDatePast_valueIsBeforeToday() {
            var ftv = FieldBuilder.of("dob", "LocalDate", List.of("@Past"));
            ftv.validValues().forEach(v ->
                    assertThat((LocalDate) v).isBefore(LocalDate.now()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALID VALUES — unannotated fields
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validValues() — unannotated fields (type defaults)")
    class ValidValuesUnannotated {

        @Test
        void string_noAnnotations_returnsNonBlankSemanticValue() {
            var ftv = FieldBuilder.of("firstName", "String", List.of());
            List<Object> values = ftv.validValues();
            assertThat(values).isNotEmpty();
            values.forEach(v -> assertThat(v.toString()).isNotBlank());
        }

        @Test
        void integer_noAnnotations_returnsPositiveValue() {
            var ftv = FieldBuilder.of("count", "Integer", List.of());
            List<Object> values = ftv.validValues();
            assertThat(values).isNotEmpty();
            values.forEach(v -> assertThat((Integer) v).isPositive());
        }

        @Test
        void boolean_noAnnotations_returnsTrue() {
            var ftv = FieldBuilder.of("active", "Boolean", List.of());
            assertThat(ftv.validValues()).contains(true);
        }

        @Test
        void long_noAnnotations_returnsPositiveValue() {
            var ftv = FieldBuilder.of("amount", "Long", List.of());
            ftv.validValues().forEach(v -> assertThat((Long) v).isPositive());
        }

        @Test
        void localDate_noAnnotations_returnsNonNull() {
            var ftv = FieldBuilder.of("createdAt", "LocalDate", List.of());
            assertThat(ftv.validValues()).isNotEmpty();
            ftv.validValues().forEach(v -> assertThat(v).isNotNull());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INVALID CASES — annotated fields
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("constraintCases() — annotated fields")
    class InvalidCasesAnnotated {

        @Test
        void notNull_producesNullCase() {
            var ftv = FieldBuilder.of("name", "String", List.of("@NotNull"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("NotNull")
                            && c.value() == null);
        }

        @Test
        void notBlank_producesNullAndBlankCases() {
            var ftv = FieldBuilder.of("name", "String", List.of("@NotBlank"));
            assertThat(ftv.constraintCases()).anyMatch(c -> c.value() == null);
            assertThat(ftv.constraintCases()).anyMatch(c -> "".equals(c.value()));
        }

        @Test
        void email_producesInvalidFormatCase() {
            var ftv = FieldBuilder.of("email", "String",
                    List.of("@NotBlank", "@Email"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Email")
                            && !c.value().toString().contains("@"));
        }

        @Test
        void minConstraint_producesBelowMinCase() {
            var ftv = FieldBuilder.of("age", "Integer", List.of("@Min(18)"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Min")
                            && (Integer) c.value() < 18);
        }

        @Test
        void maxConstraint_producesAboveMaxCase() {
            var ftv = FieldBuilder.of("age", "Integer", List.of("@Max(120)"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Max")
                            && (Integer) c.value() > 120);
        }

        @Test
        void sizeConstraint_producesTooShortAndTooLong() {
            var ftv = FieldBuilder.of("name", "String",
                    List.of("@NotBlank", "@Size(min=5, max=20)"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Size")
                            && c.value() != null
                            && c.value().toString().length() < 5);
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Size")
                            && c.value() != null
                            && c.value().toString().length() > 20);
        }

        @Test
        void negativeConstraint_producesPositiveCase() {
            // @Negative means negatives are valid — so positive is invalid
            var ftv = FieldBuilder.of("offset", "Integer", List.of("@Negative"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Negative")
                            && (Integer) c.value() > 0);
        }

        @Test
        void assertTrue_producesFalseCase() {
            var ftv = FieldBuilder.of("active", "Boolean", List.of("@AssertTrue"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("AssertTrue")
                            && Boolean.FALSE.equals(c.value()));
        }

        @Test
        void past_producesNotPastCase() {
            var ftv = FieldBuilder.of("dob", "LocalDate", List.of("@Past"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Past")
                            && ((LocalDate) c.value()).isAfter(LocalDate.now()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INVALID CASES — unannotated fields (type-level defaults)
    // This is the new behaviour: every field has a natural invalid state
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("constraintCases() — unannotated fields (type defaults)")
    class InvalidCasesUnannotated {

        @Test
        void string_noAnnotations_nullIsInvalid() {
            var ftv = FieldBuilder.of("firstName", "String", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Null")
                            && c.value() == null);
        }

        @Test
        void string_noAnnotations_blankIsInvalid() {
            var ftv = FieldBuilder.of("firstName", "String", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Blank")
                            && "".equals(c.value()));
        }

        @Test
        void integer_noAnnotations_negativeIsInvalid() {
            var ftv = FieldBuilder.of("count", "Integer", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Negative")
                            && (Integer) c.value() < 0);
        }

        @Test
        void long_noAnnotations_negativeIsInvalid() {
            var ftv = FieldBuilder.of("amount", "Long", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("Negative")
                            && (Long) c.value() < 0);
        }

        @Test
        void boolean_noAnnotations_falseIsInvalid() {
            var ftv = FieldBuilder.of("active", "Boolean", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.constraint().equals("False")
                            && Boolean.FALSE.equals(c.value()));
        }

        @Test
        void localDate_noAnnotations_nullIsInvalid() {
            var ftv = FieldBuilder.of("createdAt", "LocalDate", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.value() == null);
        }

        @Test
        void uuid_noAnnotations_nullIsInvalid() {
            var ftv = FieldBuilder.of("id", "UUID", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.value() == null);
        }

        @Test
        void integer_noAnnotations_hasAtLeastOneInvalidCase() {
            var ftv = FieldBuilder.of("score", "Integer", List.of());
            assertThat(ftv.constraintCases()).isNotEmpty();
        }

        @Test
        void string_noAnnotations_hasAtLeastTwoInvalidCases() {
            // null + blank = at least 2
            var ftv = FieldBuilder.of("description", "String", List.of());
            assertThat(ftv.constraintCases()).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NO DUPLICATE CASES
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("No duplicate invalid cases")
    class NoDuplicates {

        @Test
        void annotatedString_noDuplicateNullCase() {
            // @NotNull present: annotation layer emits null case
            // type-default layer: nullable() = false so skipped
            // Result: exactly one null case
            var ftv = FieldBuilder.of("name", "String", List.of("@NotNull"));
            long nullCount = ftv.constraintCases().stream()
                    .filter(c -> c.value() == null).count();
            assertThat(nullCount).isEqualTo(1);
        }

        @Test
        void annotatedString_noDuplicateBlankCase() {
            var ftv = FieldBuilder.of("name", "String", List.of("@NotBlank"));
            long blankCount = ftv.constraintCases().stream()
                    .filter(c -> "".equals(c.value())).count();
            assertThat(blankCount).isEqualTo(1);
        }

        @Test
        void allSuffixes_areUnique() {
            var ftv = FieldBuilder.of("email", "String",
                    List.of("@NotBlank", "@Email", "@Size(min=8, max=50)"));
            List<String> suffixes = ftv.constraintCases().stream()
                    .map(ConstraintCase::testNameSuffix).toList();
            assertThat(suffixes).doesNotHaveDuplicates();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SOURCE CODE
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validSourceCode() and sourceCode on cases")
    class SourceCode {

        @Test
        void string_quotedLiterals() {
            var ftv = FieldBuilder.of("name", "String", List.of("@NotBlank"));
            ftv.validSourceCode().forEach(s ->
                    assertThat(s).startsWith("\"").endsWith("\""));
        }

        @Test
        void integer_noQuotes() {
            var ftv = FieldBuilder.of("age", "Integer",
                    List.of("@Min(18)", "@Max(120)"));
            ftv.validSourceCode().forEach(s ->
                    assertThat(s).doesNotStartWith("\""));
        }

        @Test
        void null_sourceCode_isLiteralNull() {
            var ftv = FieldBuilder.of("name", "String", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> "null".equals(c.sourceCode()));
        }

        @Test
        void long_hasSuffix() {
            var ftv = FieldBuilder.of("amount", "Long",
                    List.of("@Min(1)", "@Max(1000)"));
            ftv.validSourceCode().forEach(s ->
                    assertThat(s).endsWith("L"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // typeDefault FLAG — annotation-driven vs type-level defaults
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("typeDefault flag")
    class TypeDefaultFlag {

        @Test
        void annotationDrivenCases_areNotTypeDefault() {
            var ftv = FieldBuilder.of("email", "String",
                    List.of("@NotBlank", "@Email"));
            ftv.constraintCases().stream()
                    .filter(c -> !c.typeDefault())
                    .forEach(c -> assertThat(c.isAnnotationDriven()).isTrue());
        }

        @Test
        void unannotatedNull_isTypeDefault() {
            var ftv = FieldBuilder.of("name", "String", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.value() == null && c.typeDefault());
        }

        @Test
        void unannotatedBlank_isTypeDefault() {
            var ftv = FieldBuilder.of("name", "String", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> "".equals(c.value()) && c.typeDefault());
        }

        @Test
        void unannotatedNegative_isTypeDefault() {
            var ftv = FieldBuilder.of("count", "Integer", List.of());
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.typeDefault() && c.value() instanceof Integer i && i < 0);
        }

        @Test
        void annotatedNull_isNotTypeDefault() {
            // @NotNull drives the null case — annotation-driven
            var ftv = FieldBuilder.of("name", "String", List.of("@NotNull"));
            assertThat(ftv.constraintCases())
                    .anyMatch(c -> c.value() == null && !c.typeDefault());
        }

        @Test
        void annotationDrivenCases_filterable() {
            var ftv = FieldBuilder.of("email", "String",
                    List.of("@NotBlank", "@Email", "@Size(min=8, max=50)"));
            // All cases for an annotated field should be annotation-driven
            assertThat(ftv.constraintCases())
                    .allMatch(c -> !c.typeDefault());
        }
    }
}
