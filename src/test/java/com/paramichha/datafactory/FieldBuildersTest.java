package com.paramichha.datafactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Field builders for typed, UUID, digit, decimal and instant fields, and string generation. */
@DisplayName("field builders")
class FieldBuildersTest {

    @Nested
    @DisplayName("field builders")
    class FieldBuilders {

        @Test
        void typedBuildersPassEveryCallThrough() {
            assertThat(DataFactory.doubles().with("@Positive").validList())
                    .allSatisfy(v -> assertThat(v).isPositive());
            assertThat(DataFactory.doubles().positive().invalid()).isLessThanOrEqualTo(0d);
            assertThat(DataFactory.floats().positive().invalid()).isLessThanOrEqualTo(0f);
            assertThat(DataFactory.decimal().positive().invalid()).isLessThanOrEqualTo(BigDecimal.ZERO);
            assertThat(DataFactory.string().notBlank().invalid()).isNullOrEmpty();
            assertThat(DataFactory.date().future().invalidList())
                    .allSatisfy(d -> assertThat(d).isBefore(LocalDate.now().plusDays(1)));
            assertThat(DataFactory.dateTime().with("@Future").valid()).isAfter(LocalDateTime.now());
            assertThat(DataFactory.dateTime().future().invalid()).isBefore(LocalDateTime.now());
        }

        @Test
        void uuidStrings() {
            FieldValue<String> uuid =
                    DataFactory.field(String.class).with(org.hibernate.validator.constraints.UUID.class);
            assertThat(java.util.UUID.fromString(uuid.valid())).isNotNull();
            assertThat(uuid.invalidList()).contains("not-a-uuid");
        }

        @Test
        void digitsOnAString() {
            FieldValue<String> digits = DataFactory.field(String.class).with("@Digits(integer=3, fraction=2)");
            assertThat(digits.validList()).allSatisfy(v -> assertThat(v).matches("\\d{1,3}\\.\\d{2}"));
            assertThat(digits.invalidList()).contains("11111");
        }

        @Test
        void decimalMaxOnAString() {
            assertThat(DataFactory.field(String.class)
                            .with("@DecimalMax(\"10\")")
                            .invalidList())
                    .contains("20");
        }

        @Test
        void instantsOutsideTheirTimeRule() {
            Instant now = Instant.now();
            assertThat(DataFactory.field(Instant.class).with("@Future").invalidList())
                    .anySatisfy(i -> assertThat(i).isBefore(now));
            assertThat(DataFactory.field(Instant.class).with("@Past").invalidList())
                    .anySatisfy(i -> assertThat(i).isAfter(now));
        }

        @Test
        void zonedAndOffsetDateTimesOutsideTheirTimeRule() {
            assertThat(DataFactory.field(ZonedDateTime.class).with("@Past").invalidList())
                    .isNotEmpty()
                    .allSatisfy(t -> assertThat(t).isAfter(ZonedDateTime.now()));
            assertThat(DataFactory.field(OffsetDateTime.class).with("@Past").invalidList())
                    .isNotEmpty()
                    .allSatisfy(t -> assertThat(t).isAfter(OffsetDateTime.now()));
            assertThat(DataFactory.field(OffsetDateTime.class).with("@Future").invalidList())
                    .isNotEmpty()
                    .allSatisfy(t -> assertThat(t).isBefore(OffsetDateTime.now()));
        }

        @Test
        void aFieldWithoutConstraintsHasNoSingleInvalidValue() {
            assertThat(DataFactory.field(String.class).invalid()).isNull();
        }
    }

    @Nested
    @DisplayName("string generation")
    class Strings {

        @Test
        void aPatternKeepsItsShapeAtSizeBoundaries() {
            assertThat(DataFactory.field(String.class)
                            .with("@Pattern(regexp=\"[a-z]{3}\")")
                            .with("@Size(min=2, max=5)")
                            .validList())
                    .allSatisfy(v -> assertThat(v).matches("[a-z]{3}"));
        }

        @Test
        void aUrlIsPaddedToItsMinimumSize() {
            assertThat(DataFactory.field(String.class)
                            .with(org.hibernate.validator.constraints.URL.class)
                            .with("@Size(min=80)")
                            .valid())
                    .hasSizeGreaterThanOrEqualTo(80);
        }

        @Test
        void namesPickTheirValue() {
            assertThat(DataFactory.string("price").valid()).matches("[0-9.,]+");
            assertThat(DataFactory.string("paragraph").valid()).contains(" ");
            assertThat(DataFactory.string("mac_address").valid()).matches("[0-9.]+");
        }
    }
}
