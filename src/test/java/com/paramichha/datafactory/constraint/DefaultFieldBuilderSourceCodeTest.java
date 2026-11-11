package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.builder.ValueBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/** Built values of each type keep their type and stay inside their constraints. */
@DisplayName("DefaultFieldBuilder — built values by type")
class DefaultFieldBuilderSourceCodeTest {

    @Nested
    @DisplayName("values of types that have no literal suffix")
    class BuiltValues {

        @Test
        void shortValuesAreShortsInRange() {
            var values = ValueBuilderFactory.create(Short.class).with("@Min(1)").with("@Max(10)").validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isBetween((short) 1, (short) 10));
        }

        @Test
        void byteValuesAreBytesInRange() {
            var values = ValueBuilderFactory.create(Byte.class).with("@Min(1)").with("@Max(5)").validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isBetween((byte) 1, (byte) 5));
        }

        @Test
        void doubleValuesAreDoublesInRange() {
            var values = ValueBuilderFactory.create(Double.class).with("@Min(1)").with("@Max(100)").validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isBetween(1d, 100d));
        }

        @Test
        void characterValuesAreSingleCharacters() {
            var values = ValueBuilderFactory.create(Character.class).validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isNotNull());
        }

        @Test
        void pastDatesAreBeforeNow() {
            var values = ValueBuilderFactory.create(Date.class).with("@Past").validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isBefore(new Date()));
        }

        @Test
        void pastYearsAreNotInTheFuture() {
            var values = ValueBuilderFactory.create(Year.class).with("@Past").validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isLessThanOrEqualTo(Year.now()));
        }

        @Test
        void pastYearMonthsAreNotInTheFuture() {
            var values = ValueBuilderFactory.create(YearMonth.class).with("@Past").validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isLessThanOrEqualTo(YearMonth.now()));
        }

        @Test
        void bigIntegerValuesAreInRange() {
            var values = ValueBuilderFactory.create(BigInteger.class).with("@Min(1)").with("@Max(100)").validList();
            assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v)
                    .isBetween(BigInteger.ONE, BigInteger.valueOf(100)));
        }
    }
}
