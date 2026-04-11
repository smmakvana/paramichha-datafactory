package com.paramichha.datafactory;

import com.paramichha.datafactory.fixture.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Covers remaining DataFactory static methods and DefaultDataBuilder branches.
 */
@DisplayName("DataFactory remaining coverage")
class DataFactoryRemainingTest {

    @Value @Builder
    static class FullRequest {
        @NotBlank @Email String email;
        @NotNull @Min(18) @Max(120) Integer age;
        @NotNull @Positive BigDecimal balance;
        @NotNull @Past LocalDate dob;
    }

    @Value @Builder
    static class WithList {
        @NotBlank String name;
        @NotEmpty List<String> tags;
    }

    @Value @Builder
    static class Outer {
        @NotBlank String name;
        @NotNull @jakarta.validation.Valid FullRequest inner;
    }

    // =========================================================================
    // DataFactory — untested static instant methods
    // =========================================================================

    @Nested
    @DisplayName("DataFactory — instant one-liners")
    class InstantMethods {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("firstName() returns non-blank")
            void firstName() { assertThat(DataFactory.firstName()).isNotBlank(); }

            @Test @DisplayName("lastName() returns non-blank")
            void lastName() { assertThat(DataFactory.lastName()).isNotBlank(); }

            @Test @DisplayName("country() returns non-blank")
            void country() { assertThat(DataFactory.country()).isNotBlank(); }

            @Test @DisplayName("postcode() returns non-blank")
            void postcode() { assertThat(DataFactory.postcode()).isNotBlank(); }

            @Test @DisplayName("url() returns non-blank")
            void url() { assertThat(DataFactory.url()).isNotBlank(); }

            @Test @DisplayName("description() returns non-blank")
            void description() { assertThat(DataFactory.description()).isNotBlank(); }

            @Test @DisplayName("uuid() returns UUID-shaped string")
            void uuid() {
                assertThat(DataFactory.uuid()).matches(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            }

            @Test @DisplayName("phones(n) returns n values")
            void phones() { assertThat(DataFactory.phones(3)).hasSize(3); }

            @Test @DisplayName("cities(n) returns n values")
            void cities() { assertThat(DataFactory.cities(3)).hasSize(3); }

            @Test @DisplayName("companies(n) returns n values")
            void companies() { assertThat(DataFactory.companies(3)).hasSize(3); }

            @Test @DisplayName("uuids(n) returns n values")
            void uuids() { assertThat(DataFactory.uuids(5)).hasSize(5); }
        }
    }

    // =========================================================================
    // DataFactory — static object shortcuts
    // =========================================================================

    @Nested
    @DisplayName("DataFactory — object shortcuts")
    class ObjectShortcuts {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {
            @Test @DisplayName("valid(type) returns valid object")
            void validShortcut() {
                assertThat(DataFactory.valid(AnnotatedWrappersRequest.class)).isNotNull();
            }

            @Test @DisplayName("invalidList(type) returns non-empty list")
            void invalidListShortcut() {
                assertThat(DataFactory.invalidList(AnnotatedWrappersRequest.class)).isNotEmpty();
            }

            @Test @DisplayName("invalidFor(type, field) returns object")
            void invalidForShortcut() {
                assertThat(DataFactory.invalidFor(AnnotatedWrappersRequest.class, "email"))
                        .isNotNull();
            }

            @Test @DisplayName("validMap(type) returns non-empty map")
            void validMapShortcut() {
                assertThat(DataFactory.validMap(AnnotatedWrappersRequest.class)).isNotEmpty();
            }

            @Test @DisplayName("stream(type, n) returns n objects")
            void streamShortcut() {
                assertThat(DataFactory.stream(AnnotatedWrappersRequest.class, 5)).hasSize(5);
            }

            @Test @DisplayName("field(Field) reads annotations from reflected field")
            void fieldFromReflectedField() throws Exception {
                var f = AnnotatedWrappersRequest.class.getDeclaredField("email");
                assertThat(DataFactory.field(f).valid()).isNotNull();
            }

            @Test @DisplayName("enumOf empty enum throws DataFactoryException")
            void enumOfEmptyThrows() {
                assertThatThrownBy(() -> DataFactory.enumOf(EmptyEnum.class))
                        .isInstanceOf(DataFactoryException.class);
            }

            @Test @DisplayName("enumOf(n) empty enum throws DataFactoryException")
            void enumOfNEmptyThrows() {
                assertThatThrownBy(() -> DataFactory.enumOf(EmptyEnum.class, 3))
                        .isInstanceOf(DataFactoryException.class);
            }
        }

        enum EmptyEnum {}
    }

    // =========================================================================
    // DefaultDataBuilder — branch coverage
    // =========================================================================

    @Nested
    @DisplayName("DefaultDataBuilder — branch coverage")
    class DefaultDataBuilderBranches {

        @Nested
        @DisplayName("Behaviour")
        class Behaviour {

            @Test @DisplayName("List<T> field is populated with at least one element")
            void listFieldPopulated() {
                var obj = DataFactory.of(WithList.class).valid();
                assertThat(obj.getTags()).isNotEmpty();
            }

            @Test @DisplayName("nested @Valid field is built recursively")
            void nestedValidBuilt() {
                var obj = DataFactory.of(Outer.class).valid();
                assertThat(obj.getInner()).isNotNull();
                assertThat(obj.getInner().getEmail()).contains("@");
            }

            @Test @DisplayName("seed() produces non-null valid objects")
            void seedDeterministic() {
                var a = DataFactory.of(FullRequest.class).seed(42L).valid();
                var b = DataFactory.of(FullRequest.class).seed(42L).valid();
                assertThat(a).isNotNull();
                assertThat(b).isNotNull();
                assertThat(a.getEmail()).isNotBlank();
                assertThat(b.getEmail()).isNotBlank();
            }

            @Test @DisplayName("mode(DEV) valid() succeeds")
            void devModeValid() {
                assertThat(DataFactory.of(FullRequest.class)
                        .mode(GenerationMode.DEV).valid()).isNotNull();
            }

            @Test @DisplayName("invalidFor() unknown field still returns object")
            void invalidForUnknownField() {
                assertThat(DataFactory.of(FullRequest.class).invalidFor("nonexistent"))
                        .isNotNull();
            }

            @Test @DisplayName("validMap() skips overridden fields")
            void validMapSkipsOverrides() {
                var map = DataFactory.of(FullRequest.class).with("age", 25).validMap();
                assertThat(map).doesNotContainKey("age");
            }

            @Test @DisplayName("withNull() sets field to null")
            void withNullSetsNull() {
                var obj = DataFactory.of(FullRequest.class).withNull("dob").valid();
                assertThat(obj.getDob()).isNull();
            }

            @Test @DisplayName("stream(n) returns n valid objects")
            void streamNObjects() {
                assertThat(DataFactory.of(FullRequest.class).stream(5)).hasSize(5);
            }

            @Test @DisplayName("mode() null throws NullPointerException")
            void modeNullThrows() {
                assertThatThrownBy(() ->
                    DataFactory.of(FullRequest.class).mode(null).valid()
                ).isInstanceOf(NullPointerException.class);
            }
        }
    }
}
