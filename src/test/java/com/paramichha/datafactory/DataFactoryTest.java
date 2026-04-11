package com.paramichha.datafactory;

import com.paramichha.datafactory.fixture.*;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Flagship test for DataFactory public API.
 *
 * Structure mirrors what TestKit generates for user classes:
 *   Construction → DataFactory.valid() builds a real, valid object
 *   Validation   → each field: valid values pass, violations detected correctly
 *   Behaviour    → invalidList(), validMap(), mode(), seed()
 *
 * Tests only via the public API — no engine internals.
 */
@DisplayName("DataFactory")
class DataFactoryTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        try (ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // ── Shared real-world fixture ─────────────────────────────────────────

    @Value @Builder
    static class UserRequest {
        @NotBlank @Email @Size(min = 6, max = 100) String email;
        @NotBlank @Size(min = 2, max = 50)         String name;
        @NotNull @Min(18) @Max(120)                Integer age;
        @NotNull @Past                             LocalDate dateOfBirth;
        @NotNull @Positive                         BigDecimal accountBalance;
    }

    @Value @Builder
    static class AddressRequest {
        @NotBlank String city;
        @NotBlank String country;
    }

    @Value @Builder
    static class OrderRequest {
        @NotBlank               String orderId;
        @NotNull @Valid         AddressRequest deliveryAddress;
        @NotEmpty               List<String> items;
    }

    static abstract class Uninstantiable { @NotBlank String value; }

    /** Used by ModeMethod tests — unannotated Integer count. */
    @Value @Builder
    static class SimpleCount { Integer count; }

    /** Used by ModeMethod tests — unannotated Integer value. */
    @Value @Builder
    static class SimpleValue { Integer value; }

    // =========================================================================
    // Construction
    // =========================================================================

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("valid instance is created")
        void validInstanceIsCreated() {
            assertThat(DataFactory.of(UserRequest.class).valid()).isNotNull();
        }

        @Test
        @DisplayName("valid instance passes validation")
        void validInstancePassesValidation() {
            assertThat(validator.validate(DataFactory.of(UserRequest.class).valid())).isEmpty();
        }

        @Test
        @DisplayName("valid instance fields are all populated")
        void validInstanceFieldsPopulated() {
            var user = DataFactory.of(UserRequest.class).valid();
            assertThat(user.getEmail()).isNotBlank().contains("@");
            assertThat(user.getName()).isNotBlank();
            assertThat(user.getAge()).isBetween(18, 120);
            assertThat(user.getDateOfBirth()).isNotNull().isBefore(LocalDate.now());
            assertThat(user.getAccountBalance()).isPositive();
        }

        @Test
        @DisplayName("nested domain object is recursively built")
        void nestedObjectBuilt() {
            var order = DataFactory.of(OrderRequest.class).valid();
            assertThat(order.getDeliveryAddress()).isNotNull();
            assertThat(order.getDeliveryAddress().getCity()).isNotBlank();
            assertThat(validator.validate(order)).isEmpty();
        }

        @Test
        @DisplayName("List field gets one valid element")
        void listFieldPopulated() {
            var order = DataFactory.of(OrderRequest.class).valid();
            assertThat(order.getItems()).isNotEmpty();
        }

        @Test
        @DisplayName("with() overrides a specific field")
        void withOverridesField() {
            var user = DataFactory.of(UserRequest.class).with("age", 25).valid();
            assertThat(user.getAge()).isEqualTo(25);
            assertThat(validator.validate(user)).isEmpty();
        }

        @Test
        @DisplayName("uninstantiable class throws DataFactoryException")
        void uninstantiableThrows() {
            assertThatThrownBy(() -> DataFactory.of(Uninstantiable.class).valid())
                    .isInstanceOf(DataFactoryException.class);
        }
    }

    // =========================================================================
    // Validation — field by field
    // =========================================================================

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Nested
        @DisplayName("email — @NotBlank @Email @Size")
        class EmailField {

            @Test
            @DisplayName("valid email passes")
            void shouldAcceptValidEmail() {
                DataFactory.string("email").email().stream(3).forEach(email -> {
                    var req = DataFactory.of(UserRequest.class).with("email", email).valid();
                    assertThat(validator.validate(req))
                            .as("email '%s' should be valid", email).isEmpty();
                });
            }

            @Test
            @DisplayName("null email fails validation")
            void shouldRejectNullEmail() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("email", null).valid()
                )).isNotEmpty();
            }

            @Test
            @DisplayName("blank email fails validation")
            void shouldRejectBlankEmail() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("email", "").valid()
                )).isNotEmpty();
            }

            @Test
            @DisplayName("invalid format email fails validation")
            void shouldRejectInvalidFormatEmail() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("email", "notanemail").valid()
                )).isNotEmpty();
            }
        }

        @Nested
        @DisplayName("age — @NotNull @Min(18) @Max(120)")
        class AgeField {

            @Test
            @DisplayName("null age fails validation")
            void shouldRejectNullAge() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("age", null).valid()
                )).isNotEmpty();
            }

            @Test
            @DisplayName("below minimum age fails validation")
            void shouldRejectBelowMinimumAge() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("age", 17).valid()
                )).isNotEmpty();
            }

            @Test
            @DisplayName("above maximum age fails validation")
            void shouldRejectAboveMaximumAge() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("age", 121).valid()
                )).isNotEmpty();
            }

            @Test
            @DisplayName("boundary age 18 passes validation")
            void shouldAcceptMinimumAge() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("age", 18).valid()
                )).isEmpty();
            }

            @Test
            @DisplayName("boundary age 120 passes validation")
            void shouldAcceptMaximumAge() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).with("age", 120).valid()
                )).isEmpty();
            }
        }
    }

    // =========================================================================
    // Behaviour — invalidList(), validMap(), mode(), seed()
    // =========================================================================

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        @Nested
        @DisplayName("violations()")
        class ViolationsMethod {

            @Test
            @DisplayName("produces non-empty list")
            void producesViolations() {
                assertThat(DataFactory.of(UserRequest.class).invalidList()).isNotEmpty();
            }

            @Test
            @DisplayName("each violation object fails validation")
            void eachViolationFails() {
                DataFactory.of(UserRequest.class).invalidList().forEach(v ->
                        assertThat(validator.validate(v))
                                .as("violation object '%s' should fail", v)
                                .isNotEmpty());
            }

            @Test
            @DisplayName("covers all constrained fields")
            void coversAllConstrainedFields() {
                var violations = DataFactory.of(UserRequest.class).invalidList();
                assertThat(violations.size()).isGreaterThanOrEqualTo(5);
            }
        }

        @Nested
        @DisplayName("validList()")
        class ValidAllMethod {

            @Test
            @DisplayName("returns map keyed by field name")
            void returnsMapByField() {
                assertThat(DataFactory.of(UserRequest.class).validMap())
                        .containsKey("email").containsKey("age");
            }

            @Test
            @DisplayName("all boundary variants pass validation")
            void allVariantsPassValidation() {
                DataFactory.of(UserRequest.class).validMap()
                        .forEach((field, variants) ->
                                variants.forEach(v ->
                                        assertThat(validator.validate(v))
                                                .as("field '%s' variant should pass", field)
                                                .isEmpty()));
            }

            @Test
            @DisplayName("age variants include boundary values 18 and 120")
            void ageIncludesBoundaries() {
                var ages = DataFactory.of(UserRequest.class).validMap()
                        .get("age").stream()
                        .map(UserRequest::getAge).toList();
                assertThat(ages).contains(18, 120);
            }

            @Test
            @DisplayName("overridden field is excluded from validAll")
            void overriddenFieldExcluded() {
                assertThat(DataFactory.of(UserRequest.class).with("age", 30).validMap())
                        .doesNotContainKey("age");
            }
        }

        @Nested
        @DisplayName("invalidFor()")
        class InvalidForMethod {

            @Test
            @DisplayName("invalidFor(email) produces email violation")
            void emailViolation() {
                var violations = validator.validate(
                        DataFactory.of(UserRequest.class).invalidFor("email"));
                assertThat(violations)
                        .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
            }

            @Test
            @DisplayName("invalidFor(age) produces age violation")
            void ageViolation() {
                var violations = validator.validate(
                        DataFactory.of(UserRequest.class).invalidFor("age"));
                assertThat(violations)
                        .anyMatch(v -> v.getPropertyPath().toString().equals("age"));
            }
        }

        @Nested
        @DisplayName("GenerationMode")
        class ModeMethod {

            @Test
            @DisplayName("PRODUCTION valid() produces positive numbers for unannotated numeric")
            void productionPositiveNumbers() {
                var obj = DataFactory.of(SimpleCount.class).mode(GenerationMode.PRODUCTION).valid();
                assertThat(obj.getCount()).isGreaterThan(0);
            }

            @Test
            @DisplayName("DEV validList() includes MIN and MAX for unannotated Integer")
            void devIncludesFullRange() {
                var all = DataFactory.of(SimpleValue.class).mode(GenerationMode.DEV).validMap();
                var values = all.get("value").stream()
                        .map(s -> ((SimpleValue) s).getValue()).toList();
                assertThat(values).contains(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
            }
        }

        @Nested
        @DisplayName("seed()")
        class SeedMethod {

            @Test
            @DisplayName("seeded object passes validation")
            void seededObjectIsValid() {
                assertThat(validator.validate(
                        DataFactory.of(UserRequest.class).seed(42L).valid()
                )).isEmpty();
            }

            @Test
            @DisplayName("seeded object fields are all populated")
            void seededFieldsPopulated() {
                var user = DataFactory.of(UserRequest.class).seed(99L).valid();
                assertThat(user.getEmail()).isNotBlank();
                assertThat(user.getAge()).isBetween(18, 120);
            }
        }
    }

    // =========================================================================
    // Fixture coverage — DataFactory against all fixture types
    // =========================================================================

    @Nested
    @DisplayName("Fixture coverage")
    class FixtureCoverage {

        @Test @DisplayName("AnnotatedPrimitivesRequest — valid passes")
        void annotatedPrimitives() {
            var obj = DataFactory.of(AnnotatedPrimitivesRequest.class).valid();
            assertThat(obj).isNotNull();
            assertThat(validator.validate(obj)).isEmpty();
        }

        @Test @DisplayName("AnnotatedWrappersRequest — valid passes")
        void annotatedWrappers() {
            var obj = DataFactory.of(AnnotatedWrappersRequest.class).valid();
            assertThat(obj).isNotNull();
            assertThat(validator.validate(obj)).isEmpty();
        }

        @Test @DisplayName("AnnotatedTemporalRequest — valid passes")
        void annotatedTemporal() {
            var obj = DataFactory.of(AnnotatedTemporalRequest.class).valid();
            assertThat(obj).isNotNull();
            assertThat(validator.validate(obj)).isEmpty();
        }

        @Test @DisplayName("CollectionsRequest — valid passes")
        void collections() {
            var obj = DataFactory.of(CollectionsRequest.class).valid();
            assertThat(obj).isNotNull();
        }

        @Test @DisplayName("PrimitivesRequest — valid passes")
        void primitives() {
            var obj = DataFactory.of(PrimitivesRequest.class).valid();
            assertThat(obj).isNotNull();
        }

        @Test @DisplayName("WrappersRequest — valid passes")
        void wrappers() {
            var obj = DataFactory.of(WrappersRequest.class).valid();
            assertThat(obj).isNotNull();
        }

        @Test @DisplayName("all fixtures — violations() non-empty for annotated")
        void annotatedFixturesHaveViolations() {
            assertThat(DataFactory.of(AnnotatedWrappersRequest.class).invalidList()).isNotEmpty();
            assertThat(DataFactory.of(AnnotatedPrimitivesRequest.class).invalidList()).isNotEmpty();
        }
    }
}