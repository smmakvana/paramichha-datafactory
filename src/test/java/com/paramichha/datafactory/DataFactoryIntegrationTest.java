package com.paramichha.datafactory;

import com.paramichha.datafactory.builder.DefaultDataBuilder;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for DefaultDataBuilder — covers all methods and branches.
 *
 * Mental model:
 *   valid()      → produces one object that passes Jakarta validation
 *   validAll()   → produces boundary variants per field, all pass validation
 *   violations() → produces one invalid object per constraint, each fails validation
 *   invalidFor() → produces one object with a named field set to an invalid value
 */
@DisplayName("DataFactory — integration")
class DataFactoryIntegrationTest {

    // ── fixtures ──────────────────────────────────────────────────────────

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Value @Builder
    static class RegisterRequest {
        @NotBlank @Email @Size(min = 8, max = 100) String email;
        @NotBlank @Size(min = 2, max = 50)         String firstName;
        @NotNull @Min(18) @Max(120)                Integer age;
        @NotNull @Past                             LocalDate dateOfBirth;
        @NotNull                                   Boolean acceptedTerms;
    }

    @Value @Builder
    static class SimpleRequest {
        @NotBlank               String name;
        @NotNull @Min(1) @Max(5) Integer rating;
    }

    @Value @Builder
    static class ParentRequest {
        @NotBlank String name;
        @NotNull @Valid AddressRequest address;
    }

    @Value @Builder
    static class AddressRequest {
        @NotBlank String city;
    }

    @Value @Builder
    static class ContainerRequest {
        @NotBlank String title;
        List<AddressRequest> addresses;
    }

    @Value @Builder
    static class TagsRequest {
        @NotBlank String name;
        List<String> tags;
    }

    @Value @Builder
    static class BoolPrimitiveRequest {
        boolean active;      // unannotated — type-default violation: false
        @NotBlank String label;
    }

    static abstract class Broken {
        @NotBlank String value;
        @Min(1) @Max(10) Integer score;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DataFactory.of() and DataFactoryException
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("DataFactory.of()")
    class FactoryOf {
        @Test void returns_non_null_DataBuilder() {
            assertThat(DataFactory.of(SimpleRequest.class)).isNotNull();
        }
        @Test void returns_DefaultDataBuilder() {
            assertThat(DataFactory.of(SimpleRequest.class)).isInstanceOf(DefaultDataBuilder.class);
        }
    }

    @Nested @DisplayName("DataFactoryException")
    class ExceptionTest {

        @ParameterizedTest(name = "{0}")
        @MethodSource("constructors")
        void constructors_set_message_and_cause(String label, String msg, Throwable cause) {
            DataFactoryException ex = cause != null
                    ? new DataFactoryException(msg, cause)
                    : new DataFactoryException(msg);
            assertThat(ex.getMessage()).isEqualTo(msg);
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        static Stream<Arguments> constructors() {
            RuntimeException cause = new RuntimeException("root");
            return Stream.of(
                Arguments.of("message only",      "just message", null),
                Arguments.of("message and cause", "msg",          cause)
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // valid()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("valid()")
    class ValidMethod {

        @Test void passes_jakarta_validation() {
            assertThat(validator.validate(DataFactory.of(RegisterRequest.class).valid())).isEmpty();
        }

        @Test void all_fields_populated() {
            var req = DataFactory.of(RegisterRequest.class).valid();
            assertThat(req.getEmail()).isNotBlank();
            assertThat(req.getFirstName()).isNotBlank();
            assertThat(req.getAge()).isBetween(18, 120);
            assertThat(req.getDateOfBirth()).isNotNull();
            assertThat(req.getAcceptedTerms()).isNotNull();
        }

        @Test void email_contains_at_sign() {
            assertThat(DataFactory.of(RegisterRequest.class).valid().getEmail()).contains("@");
        }

        @Test void with_single_override() {
            var req = DataFactory.of(RegisterRequest.class).with("age", 25).valid();
            assertThat(req.getAge()).isEqualTo(25);
            assertThat(validator.validate(req)).isEmpty();
        }

        @Test void with_multiple_overrides() {
            var req = DataFactory.of(RegisterRequest.class)
                    .with("age", 30).with("firstName", "Custom").valid();
            assertThat(req.getAge()).isEqualTo(30);
            assertThat(req.getFirstName()).isEqualTo("Custom");
        }

        @Test void wraps_exception_as_DataFactoryException() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).valid())
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // resolveAllValid() structural branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("resolveAllValid() — structural branches")
    class ResolveAllValid {

        @Test void nested_valid_field_recursed() {
            var req = DataFactory.of(ParentRequest.class).valid();
            assertThat(req.getAddress()).isNotNull();
            assertThat(req.getAddress().getCity()).isNotBlank();
            assertThat(validator.validate(req)).isEmpty();
        }

        @Test void list_of_nested_produces_one_element() {
            var req = DataFactory.of(ContainerRequest.class).valid();
            assertThat(req.getAddresses()).isNotNull().hasSize(1);
            assertThat(req.getAddresses().get(0).getCity()).isNotBlank();
        }

        @Test void list_of_java_type_gets_one_element() {
            // List<String> now generates one element so @NotEmpty / @NotNull constraints are satisfiable
            assertThat(DataFactory.of(TagsRequest.class).valid().getTags()).isNotNull().hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // validAll()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("validAll()")
    class ValidAllMethod {

        @Test void returns_map_keyed_by_field_name() {
            assertThat(DataFactory.of(RegisterRequest.class).validMap())
                    .containsKey("email").containsKey("age");
        }

        @Test void single_value_fields_omitted() {
            assertThat(DataFactory.of(RegisterRequest.class).validMap())
                    .doesNotContainKey("dateOfBirth")
                    .doesNotContainKey("acceptedTerms");
        }

        @Test void overridden_field_skipped() {
            var all = DataFactory.of(RegisterRequest.class).with("age", 30).validMap();
            assertThat(all).doesNotContainKey("age");
            assertThat(all).containsKey("email");
        }

        @Test void age_includes_boundary_values() {
            var ages = DataFactory.of(RegisterRequest.class).validMap()
                    .get("age").stream().map(RegisterRequest::getAge).toList();
            assertThat(ages).contains(18, 120);
        }

        @Test void all_variants_pass_validation() {
            DataFactory.of(RegisterRequest.class).validMap()
                    .forEach((field, variants) ->
                            variants.forEach(req ->
                                    assertThat(validator.validate(req))
                                            .as("field '%s'", field).isEmpty()));
        }

        @Test void email_variants_only_email_varies() {
            var variants = DataFactory.of(RegisterRequest.class).validMap().get("email");
            assertThat(variants.stream().map(RegisterRequest::getAge).distinct().count()).isEqualTo(1);
        }

        @Test void wraps_exception() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).validMap())
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // invalidFor()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("invalidFor()")
    class InvalidForMethod {

        @ParameterizedTest(name = "field={0}")
        @MethodSource("fieldCases")
        void field_produces_violation_on_that_field(String field) {
            var violations = validator.validate(
                    DataFactory.of(RegisterRequest.class).invalidFor(field));
            assertThat(violations)
                    .as("expected violation on '%s'", field)
                    .anyMatch(v -> v.getPropertyPath().toString().equals(field));
        }

        static Stream<String> fieldCases() {
            return Stream.of("email", "age", "firstName");
        }

        @Test void only_named_field_fails() {
            var violations = validator.validate(
                    DataFactory.of(RegisterRequest.class).invalidFor("age"));
            assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("age"));
        }

        @Test void unannotated_field_falls_back_to_type_defaults() {
            assertThat(DataFactory.of(BoolPrimitiveRequest.class).invalidFor("active")).isNotNull();
        }

        @Test void nonexistent_field_returns_valid_object() {
            var req = DataFactory.of(SimpleRequest.class).invalidFor("nonExistentField");
            assertThat(req).isNotNull();
            assertThat(validator.validate(req)).isEmpty();
        }

        @Test void wraps_exception() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).invalidFor("value"))
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // violations()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("violations()")
    class ViolationsMethod {

        @Test void produces_non_empty_list() {
            assertThat(DataFactory.of(RegisterRequest.class).invalidList()).isNotEmpty();
        }

        @Test void each_scenario_fails_validation() {
            DataFactory.of(RegisterRequest.class).invalidList().forEach(scenario ->
                    assertThat(validator.validate(scenario))
                            .as("scenario '%s' should have at least one violation", scenario)
                            .isNotEmpty());
        }

        @Test void all_constrained_fields_covered() {
            // SimpleRequest: name(@NotBlank) + rating(@Min,@Max) → at least 4 scenarios
            assertThat(DataFactory.of(SimpleRequest.class).invalidList()).hasSizeGreaterThanOrEqualTo(4);
        }

        @Test void unannotated_field_produces_type_default_violations() {
            // active (boolean, no annotation) → false (type-default)
            // label (@NotBlank) → null + ""
            // total: 3
            assertThat(DataFactory.of(BoolPrimitiveRequest.class).invalidList()).hasSize(3);
        }

        @Test void wraps_exception() {
            assertThatThrownBy(() -> DataFactory.of(Broken.class).invalidList())
                    .isInstanceOf(DataFactoryException.class)
                    .hasMessageContaining("Broken");
        }
    }
}
