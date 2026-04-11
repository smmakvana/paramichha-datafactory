package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.fixture.*;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage across all 7 fixture types.
 *
 * Mental model — each fixture is verified across three scenarios:
 *   Valid     → valid() produces an object that passes Jakarta validation
 *   ValidAll  → all boundary variants pass Jakarta validation
 *   Violations → violations() produces non-null scenarios (each with an invalid field)
 */
@DisplayName("Fixture coverage — all 7 request types")
class FixtureTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private <T> void assertValid(T obj) {
        assertThat(validator.validate(obj))
                .as("Expected no violations for: %s", obj)
                .isEmpty();
    }

    private <T> void assertViolationOn(T obj, String field) {
        assertThat(validator.validate(obj))
                .as("Expected violation on '%s'", field)
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Cross-fixture parameterized: valid() passes Jakarta validation
    // ═══════════════════════════════════════════════════════════════════════

    @ParameterizedTest(name = "{0}")
    @MethodSource("annotatedFixtures")
    @DisplayName("valid() passes Jakarta validation for every annotated fixture")
    void valid_passes_validation(String label, Class<?> type) {
        assertValid(DataFactory.of(type).valid());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("annotatedFixtures")
    @DisplayName("validAll() — all boundary variants pass validation")
    void validAll_all_variants_pass(String label, Class<?> type) {
        DataFactory.of(type).validMap()
                .forEach((field, variants) ->
                        variants.forEach(v -> assertValid(v)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("annotatedFixtures")
    @DisplayName("violations() — all scenarios are non-null")
    void violations_non_null(String label, Class<?> type) {
        DataFactory.of(type).invalidList()
                .forEach(s -> assertThat(s).isNotNull());
    }

    static Stream<Arguments> annotatedFixtures() {
        return Stream.of(
            Arguments.of("4. AnnotatedPrimitivesRequest", AnnotatedPrimitivesRequest.class),
            Arguments.of("5. AnnotatedWrappersRequest",   AnnotatedWrappersRequest.class),
            Arguments.of("6. AnnotatedTemporalRequest",   AnnotatedTemporalRequest.class),
            Arguments.of("7a. ComplexPrimitivesRequest",  ComplexPrimitivesRequest.class),
            Arguments.of("7b. ComplexWrappersRequest",    ComplexWrappersRequest.class),
            Arguments.of("7c. CollectionsRequest",        CollectionsRequest.class)
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Cross-fixture: unannotated fixtures have type-default violations
    // ═══════════════════════════════════════════════════════════════════════

    @Disabled
    @ParameterizedTest(name = "{0}")
    @MethodSource("unannotatedFixtures")
    @DisplayName("violations() — unannotated fixtures produce type-default invalids")
    void violations_unannotated_typeDefaultsProduced(String label, Class<?> type) {
        var violations = DataFactory.of(type).invalidList();
        assertThat(violations).isNotEmpty();
        violations.forEach(v -> assertThat(v).isNotNull());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unannotatedFixtures")
    @DisplayName("validAll() — unannotated numeric fixtures return type-default boundaries")
    void validAll_unannotated_numericBoundaries(String label, Class<?> type) {
        // numeric fields: negative(-1), zero(0), semantic → 3 variants → appear in validAll
        // temporal fields: single semantic → not included
        // So PrimitivesRequest and WrappersRequest will be non-empty, TemporalRequest empty
        var all = DataFactory.of(type).validMap();
        // just verify it doesn't throw and all variants are non-null
        all.forEach((field, variants) -> variants.forEach(v -> assertThat(v).isNotNull()));
    }

    static Stream<Arguments> unannotatedFixtures() {
        return Stream.of(
            Arguments.of("1. PrimitivesRequest", PrimitivesRequest.class),
            Arguments.of("2. WrappersRequest",   WrappersRequest.class),
            Arguments.of("3. TemporalRequest",   TemporalRequest.class)
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 1 — PrimitivesRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("1. PrimitivesRequest — all primitives, no annotations")
    class PrimitivesFixture {

        @Test void valid_assembles() {
            assertThat(DataFactory.of(PrimitivesRequest.class).valid()).isNotNull();
        }

        @ParameterizedTest(name = "with({0}, {1})")
        @MethodSource("overrides")
        void with_overrides_field(String field, Object value) {
            var req = DataFactory.of(PrimitivesRequest.class).with(field, value).valid();
            assertThat(req).isNotNull();
        }

        static Stream<Arguments> overrides() {
            return Stream.of(
                Arguments.of("intVal",    42),
                Arguments.of("boolVal",   true),
                Arguments.of("longVal",   999L),
                Arguments.of("doubleVal", 3.14)
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 2 — WrappersRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("2. WrappersRequest — all wrapper types, no annotations")
    class WrappersFixture {

        @ParameterizedTest(name = "field {1} is a {0}")
        @MethodSource("fieldTypes")
        void valid_field_is_correct_type(Class<?> expectedType, String getter) throws Exception {
            var req = DataFactory.of(WrappersRequest.class).valid();
            var value = WrappersRequest.class.getMethod(getter).invoke(req);
            assertThat(value).isInstanceOf(expectedType);
        }

        static Stream<Arguments> fieldTypes() {
            return Stream.of(
                Arguments.of(UUID.class,       "getUuidVal"),
                Arguments.of(BigDecimal.class,  "getBigDecimalVal"),
                Arguments.of(BigInteger.class,  "getBigIntegerVal"),
                Arguments.of(String.class,      "getStringVal")
            );
        }

        @Test void with_overrides_intVal() {
            assertThat(DataFactory.of(WrappersRequest.class).with("intVal", 7).valid().getIntVal())
                    .isEqualTo(7);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 3 — TemporalRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("3. TemporalRequest — all temporal types, no annotations")
    class TemporalFixture {

        @ParameterizedTest(name = "{1} is {0}")
        @MethodSource("temporalFields")
        void valid_temporal_field_populated(Class<?> expectedType, String getter) throws Exception {
            var req = DataFactory.of(TemporalRequest.class).valid();
            var value = TemporalRequest.class.getMethod(getter).invoke(req);
            assertThat(value).isInstanceOf(expectedType);
        }

        static Stream<Arguments> temporalFields() {
            return Stream.of(
                Arguments.of(Instant.class,        "getInstant"),
                Arguments.of(LocalDate.class,       "getLocalDate"),
                Arguments.of(LocalDateTime.class,   "getLocalDateTime"),
                Arguments.of(LocalTime.class,       "getLocalTime"),
                Arguments.of(ZonedDateTime.class,   "getZonedDateTime"),
                Arguments.of(OffsetDateTime.class,  "getOffsetDateTime"),
                Arguments.of(Year.class,            "getYear"),
                Arguments.of(YearMonth.class,       "getYearMonth")
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 4 — AnnotatedPrimitivesRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("4. AnnotatedPrimitivesRequest — primitives with annotations")
    class AnnotatedPrimitivesFixture {

        @ParameterizedTest(name = "invalidFor({0}) violates {0}")
        @MethodSource("fields")
        void invalidFor_produces_violation(String field) {
            assertViolationOn(DataFactory.of(AnnotatedPrimitivesRequest.class).invalidFor(field), field);
        }

        static Stream<String> fields() {
            return Stream.of("byteVal", "shortVal", "intVal", "longVal", "floatVal", "doubleVal");
        }

        @Test void validAll_has_multiple_fields() {
            assertThat(DataFactory.of(AnnotatedPrimitivesRequest.class).validMap()).isNotEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 5 — AnnotatedWrappersRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("5. AnnotatedWrappersRequest — wrappers with full annotations")
    class AnnotatedWrappersFixture {

        @ParameterizedTest(name = "invalidFor({0}) violates {0}")
        @MethodSource("fields")
        void invalidFor_produces_violation(String field) {
            assertViolationOn(DataFactory.of(AnnotatedWrappersRequest.class).invalidFor(field), field);
        }

        static Stream<String> fields() {
            return Stream.of("byteVal", "email", "name", "bigDecimalVal", "website");
        }

        @Test void with_overrides_email() {
            assertThat(DataFactory.of(AnnotatedWrappersRequest.class)
                    .with("email", "custom@test.com").valid().getEmail())
                    .isEqualTo("custom@test.com");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 6 — AnnotatedTemporalRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("6. AnnotatedTemporalRequest — temporals with direction annotations")
    class AnnotatedTemporalFixture {

        @ParameterizedTest(name = "invalidFor({0}) violates {0}")
        @MethodSource("fields")
        void invalidFor_produces_violation(String field) {
            assertViolationOn(DataFactory.of(AnnotatedTemporalRequest.class).invalidFor(field), field);
        }

        static Stream<String> fields() {
            return Stream.of("createdAt", "dateOfBirth", "expiresAt", "graduationYear");
        }

        @Test void validAll_empty_temporal_has_single_target() {
            assertThat(DataFactory.of(AnnotatedTemporalRequest.class).validMap()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 7a — ComplexPrimitivesRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("7a. ComplexPrimitivesRequest — nested Valid + List<DomainType>")
    class ComplexPrimitivesFixture {

        @Test void nested_primitives_populated() {
            assertThat(DataFactory.of(ComplexPrimitivesRequest.class).valid().getPrimitives()).isNotNull();
        }

        @Test void list_of_nested_populated() {
            assertThat(DataFactory.of(ComplexPrimitivesRequest.class).valid().getPrimitiveSets()).isNotEmpty();
        }

        @Test void invalidFor_name() {
            assertViolationOn(DataFactory.of(ComplexPrimitivesRequest.class).invalidFor("name"), "name");
        }

        @Test void with_overrides_name() {
            assertThat(DataFactory.of(ComplexPrimitivesRequest.class).with("name", "override").valid().getName())
                    .isEqualTo("override");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 7b — ComplexWrappersRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("7b. ComplexWrappersRequest — nested Valid + AssertFalse + NegativeOrZero")
    class ComplexWrappersFixture {

        @Test void nested_wrappers_populated() {
            assertThat(DataFactory.of(ComplexWrappersRequest.class).valid().getWrappers()).isNotNull();
        }

        @Test void items_list_populated() {
            assertThat(DataFactory.of(ComplexWrappersRequest.class).valid().getItems()).isNotEmpty();
        }

        @ParameterizedTest(name = "invalidFor({0}) violates {0}")
        @MethodSource("fields")
        void invalidFor_produces_violation(String field) {
            assertViolationOn(DataFactory.of(ComplexWrappersRequest.class).invalidFor(field), field);
        }

        static Stream<String> fields() {
            return Stream.of("deleted", "name", "adjustment");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 7c — CollectionsRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("7c. CollectionsRequest — List and Collection parameters")
    class CollectionsFixture {

        @Test void name_populated() {
            assertThat(DataFactory.of(CollectionsRequest.class).valid().getName()).isNotBlank();
        }

        @Test void items_list_populated() {
            assertThat(DataFactory.of(CollectionsRequest.class).valid().getItems()).isNotEmpty();
        }

        @Test void invalidFor_name() {
            assertViolationOn(DataFactory.of(CollectionsRequest.class).invalidFor("name"), "name");
        }

        @Test void with_overrides_tags() {
            var tags = List.of("a", "b");
            assertThat(DataFactory.of(CollectionsRequest.class).with("tags", tags).valid().getTags())
                    .isEqualTo(tags);
        }
    }
}
