package com.paramichha.datafactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Level 2 typed builders (DataFactory.string(), integer(), decimal(), date(), ...): every
 * valid value meets the constraint, every invalid value breaks it, and a seed repeats values.
 */
@DisplayName("typed builders (Level 2)")
class TypedValuesTest {

    @Nested
    @DisplayName("string()")
    class Strings {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"email", "phone", "name", "firstName", "lastName", "creditCard", "iban", "bic",
                "postcode", "address", "city", "country", "company", "url", "uuid", "ipAddress", "colour",
                "username", "description", "password", "sortCode", "accountNumber", "product", "department"})
        void everyHintGivesANonBlankValue(String hint) throws Exception {
            StringValue builder = DataFactory.string();
            builder = (StringValue) StringValue.class.getMethod(hint).invoke(builder);
            assertThat(builder.valid()).isNotBlank();
        }

        @Test
        void hintsProduceTheirFormat() {
            assertThat(DataFactory.string().email().valid()).matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
            assertThat(DataFactory.string().url().valid()).startsWith("http");
            assertThat(UUID.fromString(DataFactory.string().uuid().valid())).isNotNull();
            assertThat(DataFactory.string().ipAddress().valid()).matches("\\d{1,3}(\\.\\d{1,3}){3}");
            assertThat(DataFactory.string("email").valid()).contains("@");
            assertThat(DataFactory.string().hint("city").valid()).isNotBlank();
            assertThat(DataFactory.string("shippingAddress").valid()).doesNotMatch("\\d{1,3}(\\.\\d{1,3}){3}");
        }

        @Test
        void lengthConstraintsHoldForValidAndBreakForInvalid() {
            assertThat(DataFactory.string().length(3, 6).validList())
                    .isNotEmpty().allSatisfy(v -> assertThat(v.length()).isBetween(3, 6));
            assertThat(DataFactory.string().length(3, 6).invalidList())
                    .anySatisfy(v -> assertThat(v).hasSizeLessThan(3))
                    .anySatisfy(v -> assertThat(v).hasSizeGreaterThan(6));
            assertThat(DataFactory.string().maxLength(4).validList()).allSatisfy(v -> assertThat(v.length()).isLessThanOrEqualTo(4));
            assertThat(DataFactory.string().minLength(5).validList()).allSatisfy(v -> assertThat(v.length()).isGreaterThanOrEqualTo(5));
            assertThat(DataFactory.string().notBlank().invalidList()).contains("");
        }

        @Test
        void annotationsCanBeGivenAsStringsOrClasses() {
            assertThat(DataFactory.string().with("@Size(max=2)").valid()).hasSizeLessThanOrEqualTo(2);
            assertThat(DataFactory.string().with(jakarta.validation.constraints.Email.class).valid()).contains("@");
            assertThat(DataFactory.string().with(jakarta.validation.constraints.NotBlank.class).invalidList())
                    .contains(null, "");
        }

        @Test
        @DisplayName("a hint keeps constraints and the seed set before it")
        void hintKeepsEarlierConstraintsAndSeed() {
            StringValue shortEmail = DataFactory.string().maxLength(12);
            shortEmail.seed(3L);
            String a = (shortEmail).email().valid();
            assertThat(a).hasSizeLessThanOrEqualTo(12);
            StringValue again = DataFactory.string().maxLength(12);
            again.seed(3L);
            assertThat((again).email().valid()).isEqualTo(a);
        }

        @Test
        void streamGivesTheRequestedCount() {
            assertThat(DataFactory.string().email().stream(5)).hasSize(5).allSatisfy(v -> assertThat(v).contains("@"));
        }
    }

    @Nested
    @DisplayName("whole numbers")
    class WholeNumbers {

        @Test
        void integers() {
            assertThat(DataFactory.integer().range(18, 65).validList()).allSatisfy(v -> assertThat(v).isBetween(18, 65));
            assertThat(DataFactory.integer().range(18, 65).invalidList()).contains(17, 66);
            assertThat(DataFactory.integer().min(5).toInt()).isGreaterThanOrEqualTo(5);
            assertThat(DataFactory.integer().max(5).valid()).isLessThanOrEqualTo(5);
            assertThat(DataFactory.integer().positive().validList()).allSatisfy(v -> assertThat(v).isPositive());
            assertThat(DataFactory.integer().negative().validList()).allSatisfy(v -> assertThat(v).isNegative());
            assertThat(DataFactory.integer().positive().invalid()).isLessThanOrEqualTo(0);
            assertThat(DataFactory.integer().with(jakarta.validation.constraints.PositiveOrZero.class).valid()).isNotNegative();
            assertThat(DataFactory.integer().stream(3)).hasSize(3);
        }

        @Test
        void longs() {
            assertThat(DataFactory.longVal().range(10L, 20L).validList()).allSatisfy(v -> assertThat(v).isBetween(10L, 20L));
            assertThat(DataFactory.longVal().range(10L, 20L).invalidList()).contains(9L, 21L);
            assertThat(DataFactory.longVal().min(3L).toLong()).isGreaterThanOrEqualTo(3L);
            assertThat(DataFactory.longVal().max(3L).valid()).isLessThanOrEqualTo(3L);
            assertThat(DataFactory.longVal().positive().valid()).isPositive();
            assertThat(DataFactory.longVal().negative().valid()).isNegative();
            assertThat(DataFactory.longVal().with(jakarta.validation.constraints.Positive.class).invalid()).isLessThanOrEqualTo(0L);
            assertThat(DataFactory.longVal().stream(2)).hasSize(2);
        }

        @Test
        void shorts() {
            assertThat(DataFactory.shorts().range((short) 1, (short) 9).validList())
                    .allSatisfy(v -> assertThat(v).isBetween((short) 1, (short) 9));
            assertThat(DataFactory.shorts().range((short) 1, (short) 9).invalidList()).contains((short) 0, (short) 10);
            assertThat(DataFactory.shorts().min((short) 2).toShort()).isGreaterThanOrEqualTo((short) 2);
            assertThat(DataFactory.shorts().max((short) 2).valid()).isLessThanOrEqualTo((short) 2);
            assertThat(DataFactory.shorts().positive().valid()).isPositive();
            assertThat(DataFactory.shorts().negative().valid()).isNegative();
            assertThat(DataFactory.shorts().with("@Max(4)").invalid()).isGreaterThan((short) 4);
            assertThat(DataFactory.shorts().stream(2)).hasSize(2);
        }

        @Test
        void bytes() {
            assertThat(DataFactory.bytes().range((byte) 1, (byte) 9).validList())
                    .allSatisfy(v -> assertThat(v).isBetween((byte) 1, (byte) 9));
            assertThat(DataFactory.bytes().range((byte) 1, (byte) 9).invalidList()).contains((byte) 0, (byte) 10);
            assertThat(DataFactory.bytes().min((byte) 2).toByte()).isGreaterThanOrEqualTo((byte) 2);
            assertThat(DataFactory.bytes().max((byte) 2).valid()).isLessThanOrEqualTo((byte) 2);
            assertThat(DataFactory.bytes().positive().valid()).isPositive();
            assertThat(DataFactory.bytes().negative().valid()).isNegative();
            assertThat(DataFactory.bytes().with("@Min(3)").invalid()).isLessThan((byte) 3);
            assertThat(DataFactory.bytes().stream(2)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("decimals")
    class Decimals {

        @Test
        void floatsAndDoubles() {
            assertThat(DataFactory.floats().positive().toFloat()).isPositive();
            assertThat(DataFactory.floats().negative().valid()).isNegative();
            assertThat(DataFactory.floats().positive().invalidList()).allSatisfy(v -> assertThat(v).isLessThanOrEqualTo(0f));
            assertThat(DataFactory.floats().with("@Negative").validList()).allSatisfy(v -> assertThat(v).isNegative());
            assertThat(DataFactory.floats().stream(2)).hasSize(2);
            assertThat(DataFactory.doubles().positive().toDouble()).isPositive();
            assertThat(DataFactory.doubles().negative().valid()).isNegative();
            assertThat(DataFactory.doubles().positive().invalidList()).allSatisfy(v -> assertThat(v).isLessThanOrEqualTo(0d));
            assertThat(DataFactory.doubles().with(jakarta.validation.constraints.Positive.class).valid()).isPositive();
            assertThat(DataFactory.doubles().stream(2)).hasSize(2);
        }

        @Test
        void bigDecimals() {
            BigDecimal lo = new BigDecimal("0.01"), hi = new BigDecimal("999.99");
            assertThat(DataFactory.decimal().range("0.01", "999.99").validList())
                    .allSatisfy(v -> assertThat(v).isBetween(lo, hi));
            assertThat(DataFactory.decimal().min("5").toBigDecimal()).isGreaterThanOrEqualTo(new BigDecimal("5"));
            assertThat(DataFactory.decimal().max("5").valid()).isLessThanOrEqualTo(new BigDecimal("5"));
            assertThat(DataFactory.decimal().positive().validList()).allSatisfy(v -> assertThat(v).isPositive());
            assertThat(DataFactory.decimal().negative().validList()).allSatisfy(v -> assertThat(v).isNegative());
            assertThat(DataFactory.decimal().digits(3, 2).validList())
                    .allSatisfy(v -> assertThat(v.abs()).isLessThan(new BigDecimal("1000")));
            assertThat(DataFactory.decimal().scale(2).valid()).isNotNull();
            assertThat(DataFactory.decimal().positive().invalidList()).isNotEmpty();
            assertThat(DataFactory.decimal().with("@DecimalMax(\"10\")").valid()).isLessThanOrEqualTo(BigDecimal.TEN);
            assertThat(DataFactory.decimal().stream(2)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("dates and booleans")
    class DatesAndBooleans {

        @Test
        void localDates() {
            LocalDate today = LocalDate.now();
            assertThat(DataFactory.date().past().validList()).allSatisfy(d -> assertThat(d).isBefore(today));
            assertThat(DataFactory.date().pastOrPresent().validList()).allSatisfy(d -> assertThat(d).isBeforeOrEqualTo(today));
            assertThat(DataFactory.date().future().validList()).allSatisfy(d -> assertThat(d).isAfter(today));
            assertThat(DataFactory.date().futureOrPresent().validList()).allSatisfy(d -> assertThat(d).isAfterOrEqualTo(today));
            assertThat(DataFactory.date().past().invalid()).isAfterOrEqualTo(today);
            assertThat(DataFactory.date().with("@Future").valid()).isAfter(today);
            assertThat(DataFactory.date().stream(2)).hasSize(2);
        }

        @Test
        void localDateTimes() {
            LocalDateTime now = LocalDateTime.now();
            assertThat(DataFactory.dateTime().past().validList()).allSatisfy(d -> assertThat(d).isBefore(now));
            assertThat(DataFactory.dateTime().pastOrPresent().valid()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(DataFactory.dateTime().future().validList()).allSatisfy(d -> assertThat(d).isAfter(now));
            assertThat(DataFactory.dateTime().futureOrPresent().valid()).isAfterOrEqualTo(now.minusSeconds(1));
            assertThat(DataFactory.dateTime().future().invalidList()).isNotEmpty();
            assertThat(DataFactory.dateTime().with(jakarta.validation.constraints.Past.class).valid()).isBefore(LocalDateTime.now());
            assertThat(DataFactory.dateTime().stream(2)).hasSize(2);
        }

        @Test
        void booleans() {
            assertThat(DataFactory.bool().assertTrue().validList()).containsOnly(true);
            assertThat(DataFactory.bool().assertTrue().invalid()).isFalse();
            assertThat(DataFactory.bool().assertFalse().validList()).containsOnly(false);
            assertThat(DataFactory.bool().assertFalse().invalidList()).contains(true);
            assertThat(DataFactory.bool().validList()).contains(true, false);
            assertThat(DataFactory.bool().with("@AssertTrue").valid()).isTrue();
            assertThat(DataFactory.bool().stream(4)).hasSize(4);
        }
    }

    @Test
    @DisplayName("the same seed gives the same values, for every typed builder")
    void seededBuildersRepeat() {
        List<Supplier<FieldValue<?>>> builders = List.of(
                DataFactory::integer, DataFactory::longVal, DataFactory::shorts, DataFactory::bytes,
                DataFactory::floats, DataFactory::doubles, DataFactory::decimal, DataFactory::bool,
                DataFactory::date, DataFactory::dateTime, DataFactory::string);
        for (Supplier<FieldValue<?>> b : builders) {
            assertThat(b.get().seed(11L).valid()).isEqualTo(b.get().seed(11L).valid());
        }
    }
}
