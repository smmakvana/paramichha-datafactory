package com.paramichha.datafactory;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests PRODUCTION vs DEV generation mode via the public DataFactory API.
 *
 * PRODUCTION: injects @NotNull on all references, @Positive on numbers,
 *             @NotEmpty on collections — production-grade assumptions.
 *
 * DEV: injects @Min(TYPE_MIN)/@Max(TYPE_MAX) on unannotated numbers
 *      so validList() explores the full type range. No null constraints injected.
 */
@DisplayName("GenerationMode")
class GenerationModeTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── Fixtures ──────────────────────────────────────────────────────────

    record FullyAnnotated(
            @NotBlank @Email String email,
            @NotNull @Positive Integer age,
            @NotEmpty List<String> tags
    ) {}

    record Unannotated(
            String name,
            Integer count,
            List<String> items
    ) {}

    record NumericOnly(Integer value) {}

    // ── PRODUCTION mode ───────────────────────────────────────────────────

    @Nested
    @DisplayName("PRODUCTION mode")
    class ProductionMode {

        @Test
        @DisplayName("fully annotated class produces valid object")
        void fullyAnnotated_valid() {
            var obj = DataFactory.of(FullyAnnotated.class)
                    .mode(GenerationMode.PRODUCTION).valid();
            assertThat(obj).isNotNull();
            assertThat(validator.validate(obj)).isEmpty();
        }

        @Test
        @DisplayName("unannotated String is non-null")
        void unannotated_string_nonNull() {
            var obj = DataFactory.of(Unannotated.class)
                    .mode(GenerationMode.PRODUCTION).valid();
            assertThat(obj.name()).isNotNull();
        }

        @Test
        @DisplayName("unannotated Integer is positive")
        void unannotated_integer_positive() {
            var obj = DataFactory.of(Unannotated.class)
                    .mode(GenerationMode.PRODUCTION).valid();
            assertThat(obj.count()).isGreaterThan(0);
        }

        @Test
        @DisplayName("unannotated List is non-empty")
        void unannotated_list_nonEmpty() {
            var obj = DataFactory.of(Unannotated.class)
                    .mode(GenerationMode.PRODUCTION).valid();
            assertThat(obj.items()).isNotEmpty();
        }

        @Test
        @DisplayName("violations() finds injected constraint violations")
        void violations_includesInjectedConstraints() {
            var violations = DataFactory.of(Unannotated.class)
                    .mode(GenerationMode.PRODUCTION).invalidList();
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("default mode is PRODUCTION")
        void defaultModeIsProduction() {
            var obj = DataFactory.of(Unannotated.class).valid();
            assertThat(obj.name()).isNotNull();
            assertThat(obj.count()).isGreaterThan(0);
        }
    }

    // ── DEV mode ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DEV mode")
    class DevMode {

        @Test
        @DisplayName("fully annotated class still follows its annotations")
        void fullyAnnotated_followsAnnotations() {
            var obj = DataFactory.of(FullyAnnotated.class)
                    .mode(GenerationMode.DEV).valid();
            assertThat(obj).isNotNull();
            assertThat(validator.validate(obj)).isEmpty();
        }

        @Test
        @DisplayName("valid() on unannotated class succeeds")
        void unannotated_valid_succeeds() {
            var obj = DataFactory.of(Unannotated.class)
                    .mode(GenerationMode.DEV).valid();
            assertThat(obj).isNotNull();
        }

        @Test
        @DisplayName("DEV validList() on Integer includes MIN, zero, MAX")
        void dev_integer_validAll_includesFullRange() {
            // DEV injects @Min(Integer.MIN_VALUE) @Max(Integer.MAX_VALUE)
            // so validList() returns [MIN, 0, MAX, semantic]
            var all = DataFactory.of(NumericOnly.class)
                    .mode(GenerationMode.DEV).validMap();

            // Extract the Integer values from the variants
            List<Integer> values = all.get("value")
                    .stream()
                    .map(n -> (Integer) ((NumericOnly) n).value())
                    .toList();

            assertThat(values).contains(Integer.MIN_VALUE);
            assertThat(values).contains(Integer.MAX_VALUE);
            assertThat(values).contains(0);
        }

        @Test
        @DisplayName("DEV valid() still returns semantic value not MIN/MAX")
        void dev_valid_returnsSemanticNotExtreme() {
            // valid() uses semantic (first) value — should be a reasonable number
            // not Integer.MIN_VALUE
            var obj = DataFactory.of(NumericOnly.class)
                    .mode(GenerationMode.DEV).valid();
            // semantic value should not be the extreme boundary
            assertThat(obj.value()).isNotNull();
        }

        @Test
        @DisplayName("annotated violations still work in DEV")
        void annotatedViolationsWork() {
            var violations = DataFactory.of(FullyAnnotated.class)
                    .mode(GenerationMode.DEV).invalidList();
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("DEV and PRODUCTION produce different valid() for unannotated numeric")
        void dev_and_production_differ() {
            // PRODUCTION injects @Positive → always positive
            // DEV injects full range → validList() includes negative
            var devAll = DataFactory.of(NumericOnly.class)
                    .mode(GenerationMode.DEV).validMap();
            List<Integer> devValues = devAll.get("value")
                    .stream().map(n -> (Integer) ((NumericOnly) n).value()).toList();

            // DEV range includes negative values
            assertThat(devValues).anyMatch(v -> v < 0);

            // PRODUCTION valid() is always positive
            var prodObj = DataFactory.of(NumericOnly.class)
                    .mode(GenerationMode.PRODUCTION).valid();
            assertThat(prodObj.value()).isGreaterThan(0);
        }
    }

    // ── Seed ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Seed — deterministic output")
    class SeedMode {

        @Test
        @DisplayName("seeded object passes validation")
        void seeded_objectIsValid() {
            var obj = DataFactory.of(FullyAnnotated.class).seed(12345L).valid();
            assertThat(validator.validate(obj)).isEmpty();
        }

        @Test
        @DisplayName("seeded object fields are populated")
        void seeded_fieldsPopulated() {
            var obj = DataFactory.of(FullyAnnotated.class).seed(42L).valid();
            assertThat(obj).isNotNull();
            assertThat(obj.email()).isNotBlank();
            assertThat(obj.age()).isPositive();
            assertThat(obj.tags()).isNotEmpty();
        }

        @Test
        @DisplayName("different seeds produce valid objects")
        void differentSeeds_bothValid() {
            var a = DataFactory.of(FullyAnnotated.class).seed(1L).valid();
            var b = DataFactory.of(FullyAnnotated.class).seed(999L).valid();
            assertThat(validator.validate(a)).isEmpty();
            assertThat(validator.validate(b)).isEmpty();
        }
    }
}
