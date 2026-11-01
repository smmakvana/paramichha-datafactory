package com.paramichha.datafactory;

import jakarta.validation.constraints.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DataFactoryV2Test {

    // ── fixtures ──────────────────────────────────────────────────────────────

    record Simple(@NotBlank @Size(max = 20) String name, @Min(18) @Max(120) int age) {}

    record MinAboveMax(@NotBlank String name, @Min(5) @Max(3) int quantity) {}
    record SizeAboveMax(@Size(min = 5, max = 3) String code) {}
    record PositiveNegative(@Positive @Negative Integer amount) {}
    record DecimalAboveMax(@DecimalMin("2.0") @DecimalMax("1.0") java.math.BigDecimal rate) {}
    record NullAndNotNull(@Null @NotNull String legacy) {}
    record NullPrimitive(@Null int count) {}
    record TrueAndFalse(@AssertTrue @AssertFalse Boolean flag) {}

    record NullAllowed(@Null String legacy, @NotBlank String name) {}
    record MustBeFalse(@AssertFalse boolean deleted, @NotBlank String name) {}

    /** One field DataFactory has no creator for; the rest are ordinary. */
    record WithUnsupported(@NotBlank String name, @Min(1) int age, AtomicInteger counter) {}

    /** Mixed: one conflicting field, two good ones. */
    record OneBadField(@NotBlank String name, @Min(10) @Max(1) int broken, @Email String email) {}

    interface NotAClass {}

    record JustName(@NotBlank String name) {}
    record NameAndMore(@NotBlank String name, @Min(1) int extra) {}

    // ── happy path ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        void validSucceeds() {
            DataFactoryResult<Simple> r = DataFactoryV2.valid(Simple.class);
            assertThat(r.isSuccess()).isTrue();
            assertThat(r.get().name()).isNotBlank().hasSizeLessThanOrEqualTo(20);
            assertThat(r.get().age()).isBetween(18, 120);
        }

        @Test
        void invalidListSucceedsWithOneCasePerConstraint() {
            DataFactoryResults<Simple> rs = DataFactoryV2.invalidList(Simple.class);
            assertThat(rs.isSuccess()).isTrue();
            assertThat(rs.total()).isGreaterThanOrEqualTo(4); // NotBlank, Size, Min, Max
        }

        @Test
        void nullConstraintGeneratesNull() {
            DataFactoryResult<NullAllowed> r = DataFactoryV2.valid(NullAllowed.class);
            assertThat(r.isSuccess()).isTrue();
            assertThat(r.get().legacy()).isNull();
            assertThat(r.get().name()).isNotBlank();
        }

        @Test
        void assertFalseGeneratesFalse() {
            DataFactoryResult<MustBeFalse> r = DataFactoryV2.valid(MustBeFalse.class);
            assertThat(r.isSuccess()).isTrue();
            assertThat(r.get().deleted()).isFalse();
        }
    }

    // ── conflict detection ────────────────────────────────────────────────────

    @Nested
    @DisplayName("conflicting constraints → INVALID_CONFIGURATION, never thrown")
    class Conflicts {

        @Test
        void minAboveMax() {
            assertInvalidConfiguration(DataFactoryV2.valid(MinAboveMax.class), "quantity", "impossible range");
        }

        @Test
        void sizeMinAboveMax() {
            assertInvalidConfiguration(DataFactoryV2.valid(SizeAboveMax.class), "code", "impossible range");
        }

        @Test
        void positiveAndNegative() {
            assertInvalidConfiguration(DataFactoryV2.valid(PositiveNegative.class), "amount", "impossible range");
        }

        @Test
        void decimalMinAboveMax() {
            assertInvalidConfiguration(DataFactoryV2.valid(DecimalAboveMax.class), "rate", "impossible range");
        }

        @Test
        void nullWithNotNull() {
            assertInvalidConfiguration(DataFactoryV2.valid(NullAndNotNull.class), "legacy", "@Null conflicts");
        }

        @Test
        void nullOnPrimitive() {
            assertInvalidConfiguration(DataFactoryV2.valid(NullPrimitive.class), "count", "primitive");
        }

        @Test
        void assertTrueWithAssertFalse() {
            assertInvalidConfiguration(DataFactoryV2.valid(TrueAndFalse.class), "flag", "@AssertTrue conflicts");
        }

        @Test
        void everyEntryPointReportsTheConflict() {
            var b = DataFactoryV2.of(MinAboveMax.class);
            assertThat(b.invalid().errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
            assertThat(b.invalidFor("name").errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
            assertThat(b.invalidList().failed()).allSatisfy(f ->
                    assertThat(f.errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION));
            assertThat(b.validList(3).failed()).hasSize(3).allSatisfy(f ->
                    assertThat(f.errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION));
            assertThat(b.validPerField()).doesNotContainKey("_error").containsKey("name");
        }

        @Test
        void overridingTheConflictingFieldMakesTheClassBuildable() {
            DataFactoryResult<MinAboveMax> r = DataFactoryV2.of(MinAboveMax.class).with("quantity", 4).valid();
            assertThat(r.isSuccess()).isTrue();
            assertThat(r.get().quantity()).isEqualTo(4);
        }

        private void assertInvalidConfiguration(DataFactoryResult<?> r, String field, String fragment) {
            assertThat(r.isFailure()).isTrue();
            assertThat(r.errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
            assertThat(r.reason()).contains("'" + field + "'").contains(fragment);
        }
    }

    // ── typed error classification ────────────────────────────────────────────

    @Nested
    @DisplayName("typed error types")
    class ErrorTypes {

        @Test
        void unsupportedFieldType() {
            DataFactoryResult<WithUnsupported> r = DataFactoryV2.valid(WithUnsupported.class);
            assertThat(r.errorType()).isEqualTo(ErrorType.UNSUPPORTED_CLASS_TYPE);
            assertThat(r.reason()).contains("counter");
        }

        @Test
        void interfaceIsNotInstantiable() {
            assertThat(DataFactoryV2.valid(NotAClass.class).errorType()).isEqualTo(ErrorType.NOT_INSTANTIABLE);
        }

        @Test
        void unknownFieldInInvalidFor() {
            DataFactoryResult<Simple> r = DataFactoryV2.invalidFor(Simple.class, "nope");
            assertThat(r.errorType()).isEqualTo(ErrorType.UNKNOWN_FIELD);
        }

        @Test
        void exceptionCarriesItsTypeThroughTheCauseChain() {
            var inner = new DataFactoryException(ErrorType.NOT_INSTANTIABLE, "inner");
            var outer = new DataFactoryException("outer", new RuntimeException(inner));
            assertThat(outer.errorType()).isNull();
            assertThat(DataFactoryException.errorTypeInChain(outer)).isEqualTo(ErrorType.NOT_INSTANTIABLE);
        }
    }

    // ── per-field generation ──────────────────────────────────────────────────

    @Nested
    @DisplayName("fields() — per-field, no whole instance needed")
    class PerField {

        @Test
        void unsupportedFieldFailsAloneOthersSucceed() {
            Map<String, DataFactoryResult<FieldCases>> fields = DataFactoryV2.fields(WithUnsupported.class);

            assertThat(fields).containsOnlyKeys("name", "age", "counter");
            assertThat(fields.get("counter").errorType()).isEqualTo(ErrorType.UNSUPPORTED_CLASS_TYPE);

            FieldCases name = fields.get("name").get();
            assertThat(name.validValues()).isNotEmpty().allSatisfy(v -> assertThat((String) v).isNotBlank());
            assertThat(name.invalidCases()).extracting(c -> c.constraint()).contains("NotBlank");

            FieldCases age = fields.get("age").get();
            assertThat(age.validValues()).allSatisfy(v -> assertThat((Integer) v).isGreaterThanOrEqualTo(1));
            assertThat(age.invalidCases()).extracting(c -> c.constraint()).containsExactly("Min");
        }

        @Test
        void conflictingFieldFailsAloneOthersSucceed() {
            Map<String, DataFactoryResult<FieldCases>> fields = DataFactoryV2.fields(OneBadField.class);
            assertThat(fields.get("broken").errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
            assertThat(fields.get("name").isSuccess()).isTrue();
            assertThat(fields.get("email").isSuccess()).isTrue();
            assertThat(fields.get("email").get().invalidCases()).extracting(c -> c.constraint()).contains("Email");
        }

        @Test
        void wholeObjectStillFailsHonestly() {
            // fields() isolates the bad field, but no valid instance exists — valid() must say so.
            assertThat(DataFactoryV2.valid(OneBadField.class).errorType()).isEqualTo(ErrorType.INVALID_CONFIGURATION);
        }

        @Test
        void seededFieldsAreDeterministic() {
            var a = DataFactoryV2.of(Simple.class).seed(42).fields();
            var b = DataFactoryV2.of(Simple.class).seed(42).fields();
            assertThat(a.get("name").get().validValues()).isEqualTo(b.get("name").get().validValues());
            assertThat(a.get("age").get().validValues()).isEqualTo(b.get("age").get().validValues());
        }

        @Test
        void fieldValuesDoNotDependOnOtherFields() {
            List<Object> alone = DataFactoryV2.of(JustName.class).seed(7).fields().get("name").get().validValues();
            List<Object> withMore = DataFactoryV2.of(NameAndMore.class).seed(7).fields().get("name").get().validValues();
            assertThat(alone).isEqualTo(withMore);
        }
    }

    // ── validPerField shape ───────────────────────────────────────────────────

    @Test
    @DisplayName("validPerField never invents an \"_error\" key for a class-level failure")
    void validPerFieldKeysAreRealFields() {
        Map<String, DataFactoryResults<WithUnsupported>> m = DataFactoryV2.validPerField(WithUnsupported.class);
        assertThat(m).doesNotContainKey("_error");
        assertThat(m.values()).allSatisfy(rs -> assertThat(rs.isFailure()).isTrue());
    }
}
