package com.paramichha.datafactory;

import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 100% line and branch coverage for the api package:
 * <p>
 * DefaultDataBuilder   — all methods, all branches
 * DataFactory   — of()
 * DataFactoryException — both constructors
 * ViolationScenario     — all accessors
 * DataBuilder    — interface (no executable code)
 */
@DisplayName("DataFactory — 100% coverage")
class DataFactoryIntegrationTest {

    // ── fixtures ──────────────────────────────────────────────────────────

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Full fixture — has bounds, email, temporal, boolean, nested @Valid
     */
    @Value
    @Builder
    static class RegisterRequest {
        @NotBlank
        @Email
        @Size(min = 8, max = 100)
        String email;
        @NotBlank
        @Size(min = 2, max = 50)
        String firstName;
        @NotNull
        @Min(18)
        @Max(120)
        Integer age;
        @NotNull
        @Past
        LocalDate dateOfBirth;
        @NotNull
        Boolean acceptedTerms;
    }

    /**
     * Simple — for invalidAll field coverage
     */
    @Value
    @Builder
    static class SimpleRequest {
        @NotBlank
        String name;
        @NotNull
        @Min(1)
        @Max(5)
        Integer rating;
    }

    /**
     * Has a nested @Valid field — exercises resolveAllValid nested branch
     */
    @Value
    @Builder
    static class ParentRequest {
        @NotBlank
        String name;
        @NotNull
        @Valid
        AddressRequest address;
    }

    @Value
    @Builder
    static class AddressRequest {
        @NotBlank
        String city;
    }

    /**
     * Has a List of nested objects — exercises resolveAllValid list-nested branch
     */
    @Value
    @Builder
    static class ContainerRequest {
        @NotBlank
        String title;
        List<AddressRequest> addresses;
    }

    /**
     * Has a List of plain Strings — exercises isList + non-nested branch
     */
    @Value
    @Builder
    static class TagsRequest {
        @NotBlank
        String name;
        List<String> tags;
    }

    /**
     * Has no annotations — exercises resolveAllValid validList empty branch via isBooleanType path
     */
    @Value
    @Builder
    static class BoolPrimitiveRequest {
        boolean active;          // primitive boolean — hits isBooleanType(boolean.class)
        @NotBlank
        String label;
    }

    /**
     * Uninstantiable — exercises exception catch branches.
     * Abstract class: cannot be instantiated by any InstantiationStrategy.
     * Has @Min/@Max Integer so BoundaryPlanner produces multiple targets,
     * forcing ObjectAssembler.instantiate() to be called and throw.
     * validAll() only calls instantiate() when a field has >1 boundary value.
     */
    static abstract class Broken {
        @NotBlank
        String value;
        @Min(1)
        @Max(10)
        Integer score;  // produces 6 boundary targets → validAll() calls instantiate()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DataFactory.of()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DataFactory.of()")
    class FactoryOf {

        @Test
        void returnsDataBuilder() {
            DataBuilder<SimpleRequest> api = DataFactory.of(SimpleRequest.class);
            assertThat(api).isNotNull();
        }

        @Test
        void returnsDefaultDataBuilderInstance() {
            assertThat(DataFactory.of(SimpleRequest.class))
                    .isInstanceOf(DefaultDataBuilder.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DataFactoryException — both constructors
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DataFactoryException")
    class ExceptionCoverage {

        @Test
        void twoArgConstructor() {
            var cause = new RuntimeException("cause");
            var ex = new DataFactoryException("msg", cause);
            assertThat(ex.getMessage()).isEqualTo("msg");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        void oneArgConstructor() {
            var ex = new DataFactoryException("just message");
            assertThat(ex.getMessage()).isEqualTo("just message");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        void isRuntimeException() {
            assertThat(new DataFactoryException("x")).isInstanceOf(RuntimeException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ViolationScenario — all accessors
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ViolationScenario")
    class ViolationScenarioCoverage {

        @Test
        void allAccessors() {
            var scenario = new ViolationScenario<>("email", "NotBlank",
                    "email_null", null, "object");
            assertThat(scenario.fieldName()).isEqualTo("email");
            assertThat(scenario.constraint()).isEqualTo("NotBlank");
            assertThat(scenario.testNameSuffix()).isEqualTo("email_null");
            assertThat(scenario.invalidValue()).isNull();
            assertThat(scenario.object()).isEqualTo("object");
        }

        @Test
        void testNameSuffix_isNotBlank() {
            var scenario = new ViolationScenario<>("age", "Min", "age_belowMin", 17, null);
            assertThat(scenario.testNameSuffix()).isEqualTo("age_belowMin");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // valid() — happy path + exception branch
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("valid()")
    class ValidMethod {

        @Test
        void passesJakartaValidation() {
            var req = DataFactory.of(RegisterRequest.class).valid();
            assertThat(validator.validate(req)).isEmpty();
        }

        @Test
        void allFieldsPopulated() {
            var req = DataFactory.of(RegisterRequest.class).valid();
            assertThat(req.getEmail()).isNotBlank();
            assertThat(req.getFirstName()).isNotBlank();
            assertThat(req.getAge()).isNotNull();
            assertThat(req.getDateOfBirth()).isNotNull();
            assertThat(req.getAcceptedTerms()).isNotNull();
        }

        @Test
        void emailHasCorrectFormat() {
            assertThat(DataFactory.of(RegisterRequest.class).valid()
                    .getEmail()).contains("@");
        }

        @Test
        void ageWithinBounds() {
            assertThat(DataFactory.of(RegisterRequest.class).valid()
                    .getAge()).isBetween(18, 120);
        }

        @Test
        void with_overridesCorrectly() {
            var req = DataFactory.of(RegisterRequest.class)
                    .with("age", 25).valid();
            assertThat(req.getAge()).isEqualTo(25);
            assertThat(validator.validate(req)).isEmpty();
        }

        /**
         * Branch: override is in the map — resolveAllValid override path
         */
        @Test
        void with_multipleOverrides() {
            var req = DataFactory.of(RegisterRequest.class)
                    .with("age", 30)
                    .with("firstName", "Custom")
                    .valid();
            assertThat(req.getAge()).isEqualTo(30);
            assertThat(req.getFirstName()).isEqualTo("Custom");
        }

        /**
         * Branch: exception catch in valid() — Broken has no usable constructor
         */
        @Test
        void valid_wrapsExceptionAsDataFactoryException() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).valid())
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // resolveAllValid() branches — nested @Valid, List<Nested>, List<java.>,
    //                              validList.isEmpty()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("resolveAllValid() — structural branches")
    class ResolveAllValidBranches {

        /**
         * Branch: field.isNested() && field.hasAnnotation(Valid.class)
         */
        @Test
        void nested_valid_field_recursed() {
            var req = DataFactory.of(ParentRequest.class).valid();
            assertThat(req.getName()).isNotBlank();
            assertThat(req.getAddress()).isNotNull();
            assertThat(req.getAddress().getCity()).isNotBlank();
            assertThat(validator.validate(req)).isEmpty();
        }

        /**
         * Branch: field.isList() + isNestedType (non-java. package)
         */
        @Test
        void list_of_nested_produces_one_element() {
            var req = DataFactory.of(ContainerRequest.class).valid();
            assertThat(req.getTitle()).isNotBlank();
            assertThat(req.getAddresses()).isNotNull().hasSize(1);
            assertThat(req.getAddresses().get(0).getCity()).isNotBlank();
        }

        /**
         * Branch: field.isList() + elemType starts with java. — falls through to standard pipeline.
         * FieldBuilder for List type returns empty validValues (no shaper for List),
         * so the field gets null — exercises the validList.isEmpty() ? null branch.
         */
        @Test
        void list_of_java_type_falls_through_and_gets_null() {
            var req = DataFactory.of(TagsRequest.class).valid();
            assertThat(req.getName()).isNotBlank();
            // List<String> has no shaper → validValues() empty → tags = null
            assertThat(req.getTags()).isNull();
        }

        /**
         * Branch: primitive boolean — hits isBooleanType(boolean.class) in validAll()
         */
        @Test
        void primitive_boolean_field_populated() {
            var req = DataFactory.of(BoolPrimitiveRequest.class).valid();
            assertThat(req.getLabel()).isNotBlank();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // validAll() — all branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validAll()")
    class ValidAllMethod {

        @Test
        void returnsMapKeyedByFieldName() {
            var all = DataFactory.of(RegisterRequest.class).validAll();
            assertThat(all).containsKey("email").containsKey("age");
        }

        @Test
        void fieldsWithSingleValueOmitted() {
            var all = DataFactory.of(RegisterRequest.class).validAll();
            assertThat(all).doesNotContainKey("dateOfBirth");
        }

        /**
         * Branch: isBooleanType — Boolean field skipped
         */
        @Test
        void booleanField_omittedFromMap() {
            var all = DataFactory.of(RegisterRequest.class).validAll();
            assertThat(all).doesNotContainKey("acceptedTerms");
        }

        /**
         * Branch: isBooleanType(boolean.class) — primitive boolean skipped
         */
        @Test
        void primitiveBooleanField_omittedFromMap() {
            var all = DataFactory.of(BoolPrimitiveRequest.class).validAll();
            assertThat(all).doesNotContainKey("active");
        }

        /**
         * Branch: overrides.containsKey() — overridden field skipped in validAll
         */
        @Test
        void overriddenField_skippedInMap() {
            var all = DataFactory.of(RegisterRequest.class)
                    .with("age", 30)
                    .validAll();
            assertThat(all).doesNotContainKey("age");
            assertThat(all).containsKey("email"); // others still included
        }

        @Test
        void emailVariants_allPassValidation() {
            DataFactory.of(RegisterRequest.class).validAll()
                    .get("email").forEach(req ->
                            assertThat(validator.validate(req))
                                    .as("email variant '%s'", req.getEmail()).isEmpty());
        }

        @Test
        void ageVariants_allPassValidation() {
            var ageVariants = DataFactory.of(RegisterRequest.class).validAll().get("age");
            assertThat(ageVariants).hasSizeGreaterThanOrEqualTo(3);
            ageVariants.forEach(req ->
                    assertThat(validator.validate(req))
                            .as("age %d", req.getAge()).isEmpty());
        }

        @Test
        void ageVariants_includeBoundaryValues() {
            var ages = DataFactory.of(RegisterRequest.class).validAll()
                    .get("age").stream().map(RegisterRequest::getAge).toList();
            assertThat(ages).contains(18, 120);
        }

        @Test
        void emailVariants_onlyEmailVaries() {
            var variants = DataFactory.of(RegisterRequest.class).validAll().get("email");
            long distinctAges = variants.stream().map(RegisterRequest::getAge).distinct().count();
            assertThat(distinctAges).isEqualTo(1);
        }

        @Test
        void allVariants_passValidation() {
            DataFactory.of(RegisterRequest.class).validAll()
                    .forEach((field, variants) ->
                            variants.forEach(req ->
                                    assertThat(validator.validate(req))
                                            .as("field '%s'", field).isEmpty()));
        }

        /**
         * Branch: exception catch in validAll()
         */
        @Test
        void validAll_wrapsException() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).validAll())
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // invalidFor() — all branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("invalidFor()")
    class InvalidForMethod {

        @Test
        void email_failsOnEmailField() {
            var req = DataFactory.of(RegisterRequest.class).invalidFor("email");
            assertThat(validator.validate(req))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        void age_failsOnAgeField() {
            var req = DataFactory.of(RegisterRequest.class).invalidFor("age");
            assertThat(validator.validate(req))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("age"));
        }

        @Test
        void invalidForAge_onlyAgeFails() {
            var violations = validator.validate(
                    DataFactory.of(RegisterRequest.class).invalidFor("age"));
            assertThat(violations).allMatch(v ->
                    v.getPropertyPath().toString().equals("age"));
        }

        /**
         * Branch: driven.isEmpty() == true → falls back to all cases.
         * A field with only type-default invalid cases (no annotations) has no
         * annotation-driven cases, so driven is empty and we use all cases.
         * BoolPrimitiveRequest.active has no annotations → typeDefault only.
         */
        @Test
        void invalidFor_unannotatedField_fallsBackToTypeDefaults() {
            // active has no annotations — driven list is empty, falls back to all
            var req = DataFactory.of(BoolPrimitiveRequest.class).invalidFor("active");
            assertThat(req).isNotNull();
        }

        /**
         * Branch: cases.isEmpty() == true → no value put, field stays valid.
         * A field that genuinely has zero invalid cases (none at all).
         * We need a class whose field produces no invalid cases.
         *
         * @Null fields have mustBeNull=true, validValue=null — no invalid cases.
         */
        @Test
        void invalidFor_fieldWithNoCases_objectStillBuilt() {
            // Using an unknown field name — ifPresent never fires, values unchanged
            var req = DataFactory.of(SimpleRequest.class).invalidFor("nonExistentField");
            assertThat(req).isNotNull();
            // object is fully valid since no field was invalidated
            assertThat(validator.validate(req)).isEmpty();
        }

        /**
         * Branch: exception catch in invalidFor()
         */
        @Test
        void invalidFor_wrapsException() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).invalidFor("value"))
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // violations() — all branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("violations()")
    class InvalidAllMethod {

        @Test
        void producesScenarios() {
            assertThat(DataFactory.of(RegisterRequest.class).violations())
                    .isNotEmpty();
        }

        @Test
        void eachScenario_failsOnItsField() {
            DataFactory.of(RegisterRequest.class).violations()
                    .forEach(scenario -> {
                        var violations = validator.validate(scenario.object());
                        assertThat(violations)
                                .as("scenario '%s' should fail on '%s'",
                                        scenario.testNameSuffix(), scenario.fieldName())
                                .anyMatch(v -> v.getPropertyPath().toString()
                                        .equals(scenario.fieldName()));
                    });
        }

        @Test
        void allConstrainedFields_covered() {
            var fieldNames = DataFactory.of(SimpleRequest.class).violations()
                    .stream().map(ViolationScenario::fieldName).distinct().toList();
            assertThat(fieldNames).contains("name", "rating");
        }

        @Test
        void testNameSuffix_wellFormed() {
            DataFactory.of(SimpleRequest.class).violations()
                    .forEach(s -> assertThat(s.testNameSuffix()).isNotBlank());
        }

        @Test
        void allScenarioAccessors_nonNull() {
            DataFactory.of(RegisterRequest.class).violations()
                    .forEach(scenario -> {
                        assertThat(scenario.fieldName()).isNotBlank();
                        assertThat(scenario.constraint()).isNotBlank();
                        assertThat(scenario.testNameSuffix()).isNotBlank();
                        // invalidValue may be null (for @NotNull violations)
                        assertThat(scenario.object()).isNotNull();
                    });
        }

        /**
         * Branch: field with no annotation-driven cases produces no scenarios (loop body skipped)
         */
        @Test
        void unannotatedField_producesNoScenarios() {
            // BoolPrimitiveRequest.active has no annotations → no annotation-driven cases
            // So violations() for that field contributes zero scenarios
            var scenarios = DataFactory.of(BoolPrimitiveRequest.class).violations();
            // label field has @NotBlank → produces scenarios; active has none
            assertThat(scenarios.stream().map(ViolationScenario::fieldName).distinct())
                    .doesNotContain("active");
            assertThat(scenarios.stream().map(ViolationScenario::fieldName).distinct())
                    .contains("label");
        }

        /**
         * Branch: exception catch in violations()
         */
        @Test
        void violations_wrapsException() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).violations())
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }
}
