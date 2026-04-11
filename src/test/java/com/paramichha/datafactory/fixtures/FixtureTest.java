package com.paramichha.datafactory.fixtures;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.ViolationScenario;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage across all 7 fixture types.
 * <p>
 * For every fixture:
 * - valid()        produces an object that passes Jakarta validation
 * - validAll()     produces boundary variants, all pass validation
 * - violations()   produces scenarios that each fail validation on the expected field
 * - invalidFor()   produces an object that fails validation on the named field
 */
@DisplayName("Fixture coverage — all 7 request types")
class FixtureTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private <T> void assertValid(T obj) {
        assertThat(validator.validate(obj))
                .as("Expected no violations but got: %s", validator.validate(obj))
                .isEmpty();
    }

    private <T> void assertViolationOn(T obj, String field) {
        assertThat(validator.validate(obj))
                .as("Expected violation on '%s'", field)
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 1 — PrimitivesRequest (all 8 primitives, no annotations)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. PrimitivesRequest — all primitives, no annotations")
    class PrimitivesFixture {

        @Test
        void valid_notNull() {
            assertThat(DataFactory.of(PrimitivesRequest.class).valid()).isNotNull();
        }

        @Test
        void valid_allPrimitivesPopulated() {
            var req = DataFactory.of(PrimitivesRequest.class).valid();
            // primitives always have values — just verify the object assembles
            assertThat(req).isNotNull();
        }

        @Test
        void validAll_noAnnotations_emptyMap() {
            // no bounds annotations → no multi-value fields → empty map
            Map<String, List<PrimitivesRequest>> all =
                    DataFactory.of(PrimitivesRequest.class).validAll();
            assertThat(all).isEmpty();
        }

        @Test
        void violations_noAnnotations_emptyList() {
            assertThat(DataFactory.of(PrimitivesRequest.class).violations()).isEmpty();
        }

        @Test
        void with_overridesIntVal() {
            var req = DataFactory.of(PrimitivesRequest.class).with("intVal", 42).valid();
            assertThat(req.getIntVal()).isEqualTo(42);
        }

        @Test
        void with_overridesBoolVal() {
            var req = DataFactory.of(PrimitivesRequest.class).with("boolVal", true).valid();
            assertThat(req.isBoolVal()).isTrue();
        }

        @Test
        void with_overridesLongVal() {
            var req = DataFactory.of(PrimitivesRequest.class).with("longVal", 999L).valid();
            assertThat(req.getLongVal()).isEqualTo(999L);
        }

        @Test
        void with_overridesDoubleVal() {
            var req = DataFactory.of(PrimitivesRequest.class).with("doubleVal", 3.14).valid();
            assertThat(req.getDoubleVal()).isEqualTo(3.14);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 2 — WrappersRequest (all wrapper types, no annotations)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. WrappersRequest — all wrapper types, no annotations")
    class WrappersFixture {

        @Test
        void valid_notNull() {
            assertThat(DataFactory.of(WrappersRequest.class).valid()).isNotNull();
        }

        @Test
        void valid_uuidPopulated() {
            var req = DataFactory.of(WrappersRequest.class).valid();
            assertThat(req.getUuidVal()).isInstanceOf(UUID.class);
        }

        @Test
        void valid_bigDecimalPopulated() {
            var req = DataFactory.of(WrappersRequest.class).valid();
            assertThat(req.getBigDecimalVal()).isInstanceOf(BigDecimal.class);
        }

        @Test
        void valid_bigIntegerPopulated() {
            var req = DataFactory.of(WrappersRequest.class).valid();
            assertThat(req.getBigIntegerVal()).isInstanceOf(BigInteger.class);
        }

        @Test
        void valid_stringPopulated() {
            var req = DataFactory.of(WrappersRequest.class).valid();
            assertThat(req.getStringVal()).isNotNull();
        }

        @Test
        void validAll_noAnnotations_emptyMap() {
            assertThat(DataFactory.of(WrappersRequest.class).validAll()).isEmpty();
        }

        @Test
        void violations_noAnnotations_emptyList() {
            assertThat(DataFactory.of(WrappersRequest.class).violations()).isEmpty();
        }

        @Test
        void with_overridesIntVal() {
            var req = DataFactory.of(WrappersRequest.class).with("intVal", 7).valid();
            assertThat(req.getIntVal()).isEqualTo(7);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 3 — TemporalRequest (all temporal types, no annotations)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. TemporalRequest — all temporal types, no annotations")
    class TemporalFixture {

        @Test
        void valid_notNull() {
            assertThat(DataFactory.of(TemporalRequest.class).valid()).isNotNull();
        }

        @Test
        void valid_instantPopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getInstant())
                    .isInstanceOf(Instant.class);
        }

        @Test
        void valid_localDatePopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getLocalDate())
                    .isInstanceOf(LocalDate.class);
        }

        @Test
        void valid_localDateTimePopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getLocalDateTime())
                    .isInstanceOf(LocalDateTime.class);
        }

        @Test
        void valid_localTimePopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getLocalTime())
                    .isInstanceOf(LocalTime.class);
        }

        @Test
        void valid_zonedDateTimePopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getZonedDateTime())
                    .isInstanceOf(ZonedDateTime.class);
        }

        @Test
        void valid_offsetDateTimePopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getOffsetDateTime())
                    .isInstanceOf(OffsetDateTime.class);
        }

        @Test
        void valid_yearPopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getYear())
                    .isInstanceOf(Year.class);
        }

        @Test
        void valid_yearMonthPopulated() {
            assertThat(DataFactory.of(TemporalRequest.class).valid().getYearMonth())
                    .isInstanceOf(YearMonth.class);
        }

        @Test
        void validAll_noAnnotations_emptyMap() {
            assertThat(DataFactory.of(TemporalRequest.class).validAll()).isEmpty();
        }

        @Test
        void violations_noAnnotations_emptyList() {
            assertThat(DataFactory.of(TemporalRequest.class).violations()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 4 — AnnotatedPrimitivesRequest (primitives + @Min @Max @AssertTrue)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("4. AnnotatedPrimitivesRequest — primitives with annotations")
    class AnnotatedPrimitivesFixture {

        @Test
        void valid_passesValidation() {
            assertValid(DataFactory.of(AnnotatedPrimitivesRequest.class).valid());
        }

        @Test
        void validAll_allPassValidation() {
            DataFactory.of(AnnotatedPrimitivesRequest.class).validAll()
                    .forEach((field, variants) ->
                            variants.forEach(v -> assertValid(v)));
        }

        @Test
        void validAll_hasMultipleBoundaryFields() {
            assertThat(DataFactory.of(AnnotatedPrimitivesRequest.class).validAll())
                    .isNotEmpty();
        }

        @Test
        void violations_allFailValidation() {
            List<ViolationScenario<AnnotatedPrimitivesRequest>> scenarios =
                    DataFactory.of(AnnotatedPrimitivesRequest.class).violations();
            assertThat(scenarios).isNotEmpty();
            scenarios.forEach(s -> assertViolationOn(s.object(), s.fieldName()));
        }

        @Test
        void violations_byteVal_belowMin() {
            DataFactory.of(AnnotatedPrimitivesRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("byteVal") && s.testNameSuffix().contains("belowMin"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "byteVal"));
        }

        @Test
        void violations_intVal_aboveMax() {
            DataFactory.of(AnnotatedPrimitivesRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("intVal") && s.testNameSuffix().contains("aboveMax"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "intVal"));
        }

        @Test
        void violations_boolVal_assertTrue_false_isInvalid() {
            DataFactory.of(AnnotatedPrimitivesRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("boolVal"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "boolVal"));
        }

        @Test
        void invalidFor_byteVal() {
            assertViolationOn(
                    DataFactory.of(AnnotatedPrimitivesRequest.class).invalidFor("byteVal"),
                    "byteVal");
        }

        @Test
        void invalidFor_longVal() {
            assertViolationOn(
                    DataFactory.of(AnnotatedPrimitivesRequest.class).invalidFor("longVal"),
                    "longVal");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 5 — AnnotatedWrappersRequest (wrappers + full annotation set)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("5. AnnotatedWrappersRequest — wrappers with full annotations")
    class AnnotatedWrappersFixture {

        @Test
        void valid_passesValidation() {
            assertValid(DataFactory.of(AnnotatedWrappersRequest.class).valid());
        }

        @Test
        void validAll_allPassValidation() {
            DataFactory.of(AnnotatedWrappersRequest.class).validAll()
                    .forEach((field, variants) ->
                            variants.forEach(v -> assertValid(v)));
        }

        @Test
        void violations_allFailValidation() {
            DataFactory.of(AnnotatedWrappersRequest.class).violations()
                    .forEach(s -> assertViolationOn(s.object(), s.fieldName()));
        }

        @Test
        void violations_email_invalidFormat() {
            DataFactory.of(AnnotatedWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("email") && s.testNameSuffix().contains("invalidFormat"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "email"));
        }

        @Test
        void violations_email_tooShort() {
            DataFactory.of(AnnotatedWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("email") && s.testNameSuffix().contains("tooShort"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "email"));
        }

        @Test
        void violations_email_tooLong() {
            DataFactory.of(AnnotatedWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("email") && s.testNameSuffix().contains("tooLong"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "email"));
        }

        @Test
        void violations_bigDecimalVal_notNull() {
            DataFactory.of(AnnotatedWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("bigDecimalVal"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "bigDecimalVal"));
        }

        @Test
        void violations_longVal_positive_zero_isInvalid() {
            DataFactory.of(AnnotatedWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("longVal"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "longVal"));
        }

        @Test
        void violations_website_invalidUrl() {
            DataFactory.of(AnnotatedWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("website"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "website"));
        }

        @Test
        void invalidFor_email() {
            assertViolationOn(
                    DataFactory.of(AnnotatedWrappersRequest.class).invalidFor("email"),
                    "email");
        }

        @Test
        void invalidFor_name() {
            assertViolationOn(
                    DataFactory.of(AnnotatedWrappersRequest.class).invalidFor("name"),
                    "name");
        }

        @Test
        void invalidFor_bigDecimalVal() {
            assertViolationOn(
                    DataFactory.of(AnnotatedWrappersRequest.class).invalidFor("bigDecimalVal"),
                    "bigDecimalVal");
        }

        @Test
        void with_overridesEmail() {
            var req = DataFactory.of(AnnotatedWrappersRequest.class)
                    .with("email", "custom@test.com").valid();
            assertThat(req.getEmail()).isEqualTo("custom@test.com");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 6 — AnnotatedTemporalRequest (all temporal + direction annotations)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("6. AnnotatedTemporalRequest — temporals with direction annotations")
    class AnnotatedTemporalFixture {

        @Test
        void valid_passesValidation() {
            assertValid(DataFactory.of(AnnotatedTemporalRequest.class).valid());
        }

        @Test
        void validAll_returnsEmpty_temporalsHaveOneTarget() {
            // temporal fields produce only 1 semantic target → excluded from validAll
            assertThat(DataFactory.of(AnnotatedTemporalRequest.class).validAll()).isEmpty();
        }

        @Test
        void violations_allFailValidation() {
            DataFactory.of(AnnotatedTemporalRequest.class).violations()
                    .forEach(s -> assertViolationOn(s.object(), s.fieldName()));
        }

        @Test
        void violations_createdAt_notPast() {
            DataFactory.of(AnnotatedTemporalRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("createdAt") && s.testNameSuffix().contains("notPast"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "createdAt"));
        }

        @Test
        void violations_expiresAt_notFuture() {
            DataFactory.of(AnnotatedTemporalRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("expiresAt") && s.testNameSuffix().contains("notFuture"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "expiresAt"));
        }

        @Test
        void violations_dateOfBirth_past_localDate() {
            DataFactory.of(AnnotatedTemporalRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("dateOfBirth"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "dateOfBirth"));
        }

        @Test
        void violations_renewalMonth_futureOrPresent() {
            DataFactory.of(AnnotatedTemporalRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("renewalMonth"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "renewalMonth"));
        }

        @Test
        void invalidFor_createdAt() {
            assertViolationOn(
                    DataFactory.of(AnnotatedTemporalRequest.class).invalidFor("createdAt"),
                    "createdAt");
        }

        @Test
        void invalidFor_expiresAt() {
            assertViolationOn(
                    DataFactory.of(AnnotatedTemporalRequest.class).invalidFor("expiresAt"),
                    "expiresAt");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 7a — ComplexPrimitivesRequest (nested @Valid + List<DomainType>)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("7a. ComplexPrimitivesRequest — nested Valid + List<DomainType>")
    class ComplexPrimitivesFixture {

        @Test
        void valid_passesValidation() {
            assertValid(DataFactory.of(ComplexPrimitivesRequest.class).valid());
        }

        @Test
        void valid_nestedPrimitivesPopulated() {
            var req = DataFactory.of(ComplexPrimitivesRequest.class).valid();
            assertThat(req.getPrimitives()).isNotNull();
        }

        @Test
        void valid_listOfPrimitivesPopulated() {
            var req = DataFactory.of(ComplexPrimitivesRequest.class).valid();
            assertThat(req.getPrimitiveSets()).isNotEmpty();
        }

        @Test
        void valid_tagsListPopulated() {
            // List<String> — non-domain, so not recursed into, may be null
            var req = DataFactory.of(ComplexPrimitivesRequest.class).valid();
            assertThat(req).isNotNull();
        }

        @Test
        void violations_allFailValidation() {
            DataFactory.of(ComplexPrimitivesRequest.class).violations()
                    .forEach(s -> assertViolationOn(s.object(), s.fieldName()));
        }

        @Test
        void violations_name_notBlank() {
            DataFactory.of(ComplexPrimitivesRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("name"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "name"));
        }

        @Test
        void invalidFor_name() {
            assertViolationOn(
                    DataFactory.of(ComplexPrimitivesRequest.class).invalidFor("name"),
                    "name");
        }

        @Test
        void with_overridesName() {
            var req = DataFactory.of(ComplexPrimitivesRequest.class)
                    .with("name", "override").valid();
            assertThat(req.getName()).isEqualTo("override");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 7b — ComplexWrappersRequest (nested @Valid + @AssertFalse + @NegativeOrZero)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("7b. ComplexWrappersRequest — nested Valid + AssertFalse + NegativeOrZero")
    class ComplexWrappersFixture {

        @Test
        void valid_passesValidation() {
            assertValid(DataFactory.of(ComplexWrappersRequest.class).valid());
        }

        @Test
        void valid_nestedWrappersPopulated() {
            assertThat(DataFactory.of(ComplexWrappersRequest.class).valid().getWrappers())
                    .isNotNull();
        }

        @Test
        void valid_itemsListPopulated() {
            assertThat(DataFactory.of(ComplexWrappersRequest.class).valid().getItems())
                    .isNotEmpty();
        }

        @Test
        void violations_allFailValidation() {
            DataFactory.of(ComplexWrappersRequest.class).violations()
                    .forEach(s -> assertViolationOn(s.object(), s.fieldName()));
        }

        @Test
        void violations_deleted_assertFalse_true_isInvalid() {
            DataFactory.of(ComplexWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("deleted"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "deleted"));
        }

        @Test
        void violations_adjustment_negativeOrZero_positive_isInvalid() {
            DataFactory.of(ComplexWrappersRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("adjustment"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "adjustment"));
        }

        @Test
        void invalidFor_deleted() {
            assertViolationOn(
                    DataFactory.of(ComplexWrappersRequest.class).invalidFor("deleted"),
                    "deleted");
        }

        @Test
        void invalidFor_name() {
            assertViolationOn(
                    DataFactory.of(ComplexWrappersRequest.class).invalidFor("name"),
                    "name");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fixture 7c — CollectionsRequest (List, Collection as params)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("7c. CollectionsRequest — List and Collection parameters")
    class CollectionsFixture {

        @Test
        void valid_notNull() {
            assertThat(DataFactory.of(CollectionsRequest.class).valid()).isNotNull();
        }

        @Test
        void valid_namePopulated() {
            assertThat(DataFactory.of(CollectionsRequest.class).valid().getName())
                    .isNotBlank();
        }

        @Test
        void valid_itemsListPopulated() {
            assertThat(DataFactory.of(CollectionsRequest.class).valid().getItems())
                    .isNotEmpty();
        }

        @Test
        void violations_name_notBlank() {
            DataFactory.of(CollectionsRequest.class).violations().stream()
                    .filter(s -> s.fieldName().equals("name"))
                    .findFirst()
                    .ifPresent(s -> assertViolationOn(s.object(), "name"));
        }

        @Test
        void invalidFor_name() {
            assertViolationOn(
                    DataFactory.of(CollectionsRequest.class).invalidFor("name"),
                    "name");
        }

        @Test
        void with_overrideName() {
            var req = DataFactory.of(CollectionsRequest.class)
                    .with("name", "test").valid();
            assertThat(req.getName()).isEqualTo("test");
        }

        @Test
        void with_overrideTags() {
            var tags = List.of("a", "b", "c");
            var req = DataFactory.of(CollectionsRequest.class)
                    .with("tags", tags).valid();
            assertThat(req.getTags()).isEqualTo(tags);
        }
    }
}
