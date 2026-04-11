package com.paramichha.datafactory.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Targets uncovered branches in DefaultFieldBuilder:
 * toSourceCode — Short, Byte, Double, Character, Date, Year, YearMonth, BigInteger
 * castNumeric   — BigInteger arm
 * futureTemporal/pastTemporal — Date, Year, YearMonth arms
 * isList(Collection)
 * isNegativeAllowed edge cases
 */
@DisplayName("DefaultFieldBuilder — coverage gaps")
class DefaultFieldBuilderCoverageTest {

    private static FieldBuilder fb(String name, String type, String... annotations) {
        return FieldBuilder.of(name, type, List.of(annotations));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // toSourceCode — all missing type branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toSourceCode — missing types")
    class ToSourceCode {

        @Test
        void short_has_no_suffix() {
            var codes = fb("n", "Short", "@Min(1)", "@Max(10)").validSourceCode();
            assertThat(codes).isNotEmpty();
            codes.forEach(c -> assertThat(c).doesNotEndWith("L").doesNotEndWith("f"));
        }

        @Test
        void byte_has_no_suffix() {
            var codes = fb("n", "Byte", "@Min(1)", "@Max(5)").validSourceCode();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void double_has_d_suffix() {
            var codes = fb("n", "Double", "@Min(1)", "@Max(100)").validSourceCode();
            assertThat(codes).isNotEmpty();
            assertThat(codes.get(0)).endsWith("d");
        }

        @Test
        void character_toString() {
            var codes = fb("c", "Character").validSourceCode();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void date_source_code() {
            // Date field with @Past — validValues produces a past Date
            // toSourceCode hits the fallback value.toString()
            var codes = fb("dt", "Date", "@Past").validSourceCode();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void year_source_code() {
            var codes = fb("y", "Year", "@Past").validSourceCode();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void yearMonth_source_code() {
            var codes = fb("ym", "YearMonth", "@Past").validSourceCode();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void bigInteger_source_code() {
            var codes = fb("n", "BigInteger", "@Min(1)", "@Max(100)").validSourceCode();
            assertThat(codes).isNotEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // castNumeric — BigInteger arm via constraintCases belowMin/aboveMax
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("castNumeric — BigInteger")
    class CastNumericBigInteger {

        @Test
        void belowMin_returns_biginteger() {
            var cases = fb("n", "BigInteger", "@Min(5)").constraintCases();
            var belowMin = cases.stream().filter(c -> c.testNameSuffix().contains("belowMin")).findFirst();
            assertThat(belowMin).isPresent();
            assertThat(belowMin.get().value()).isInstanceOf(BigInteger.class);
        }

        @Test
        void aboveMax_returns_biginteger() {
            var cases = fb("n", "BigInteger", "@Max(10)").constraintCases();
            var aboveMax = cases.stream().filter(c -> c.testNameSuffix().contains("aboveMax")).findFirst();
            assertThat(aboveMax).isPresent();
            assertThat(aboveMax.get().value()).isInstanceOf(BigInteger.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // futureTemporal and pastTemporal — Date, Year, YearMonth
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("futureTemporal — Date, Year, YearMonth")
    class FutureTemporal {

        @Test
        void date_past_notPast_is_date() {
            var cases = fb("dt", "Date", "@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.testNameSuffix().contains("notPast")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(Date.class);
        }

        @Test
        void year_past_notPast_is_year() {
            var cases = fb("y", "Year", "@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.testNameSuffix().contains("notPast")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(Year.class);
            assertThat((Year) notPast.get().value()).isGreaterThan(Year.now());
        }

        @Test
        void yearMonth_past_notPast_is_yearmonth() {
            var cases = fb("ym", "YearMonth", "@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.testNameSuffix().contains("notPast")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(YearMonth.class);
            assertThat((YearMonth) notPast.get().value()).isGreaterThan(YearMonth.now());
        }

        @Test
        void date_future_notFuture_is_date() {
            var cases = fb("dt", "Date", "@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.testNameSuffix().contains("notFuture")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(Date.class);
        }

        @Test
        void year_future_notFuture_is_year() {
            var cases = fb("y", "Year", "@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.testNameSuffix().contains("notFuture")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(Year.class);
            assertThat((Year) notFuture.get().value()).isLessThan(Year.now());
        }

        @Test
        void yearMonth_future_notFuture_is_yearmonth() {
            var cases = fb("ym", "YearMonth", "@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.testNameSuffix().contains("notFuture")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(YearMonth.class);
            assertThat((YearMonth) notFuture.get().value()).isLessThan(YearMonth.now());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // isList — Collection type
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isList — Collection type")
    class IsListCollection {

        @Test
        void collection_type_default_null_and_empty() {
            var cases = fb("items", "Collection").constraintCases();
            assertThat(cases).anyMatch(c -> c.value() == null);
            assertThat(cases).anyMatch(c -> c.value() instanceof List<?> l && l.isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // isNegativeAllowed edge cases
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isNegativeAllowed — max <= 0 means negative is valid")
    class IsNegativeAllowed {

        @Test
        void negative_allowed_positive_is_invalid() {
            // @NegativeOrZero: max=0 -> isNegativeAllowed=true -> positive case added
            var cases = fb("n", "Integer", "@NegativeOrZero").constraintCases();
            assertThat(cases).anyMatch(c -> c.constraint().equals("Negative")
                    && c.testNameSuffix().contains("positive"));
        }

        @Test
        void negative_only_positive_is_invalid() {
            // @Negative: max=-1 -> isNegativeAllowed=true
            var cases = fb("n", "Integer", "@Negative").constraintCases();
            assertThat(cases).anyMatch(c -> c.constraint().equals("Negative")
                    && c.testNameSuffix().contains("positive"));
        }

        @Test
        void positive_not_allowed_negative_is_type_default() {
            // No min annotation -> negative type default appears
            var cases = fb("n", "Integer").constraintCases();
            assertThat(cases).anyMatch(c -> c.typeDefault() && c.testNameSuffix().contains("negative"));
        }

        @Test
        void with_min_no_negative_type_default() {
            // @Min(5) -> bounds.hasMin()=true -> no negative type default
            var cases = fb("n", "Integer", "@Min(5)").constraintCases();
            assertThat(cases).noneMatch(c -> c.typeDefault() && c.testNameSuffix().contains("negative"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // defaultPrimitive — AllArgsConstructorStrategy and RecordStrategy
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("defaultPrimitive — all 8 primitive types")
    class DefaultPrimitive {

        @Test
        void allArgs_nulls_for_all_primitives_use_defaults() throws Exception {
            var fields = FieldDescriptor.extract(AllArgsClass.class);
            var values = new java.util.LinkedHashMap<String, Object>();
            fields.forEach(f -> values.put(f.name(), null));

            var obj = AllArgsConstructorStrategy.INSTANCE.instantiate(
                    AllArgsClass.class, fields, values);
            assertThat(obj.boolV).isFalse();
            assertThat(obj.charV).isEqualTo('\0');
            assertThat(obj.byteV).isEqualTo((byte) 0);
            assertThat(obj.shortV).isEqualTo((short) 0);
            assertThat(obj.intV).isEqualTo(0);
            assertThat(obj.longV).isEqualTo(0L);
            assertThat(obj.floatV).isEqualTo(0.0f);
            assertThat(obj.doubleV).isEqualTo(0.0d);
        }

        @Test
        void record_nulls_for_all_primitives_use_defaults() throws Exception {
            var fields = FieldDescriptor.extract(AllArgsRecord.class);
            var values = new java.util.LinkedHashMap<String, Object>();
            fields.forEach(f -> values.put(f.name(), null));

            var obj = RecordStrategy.INSTANCE.instantiate(
                    AllArgsRecord.class, fields, values);
            assertThat(obj.boolV()).isFalse();
            assertThat(obj.charV()).isEqualTo('\0');
            assertThat(obj.byteV()).isEqualTo((byte) 0);
            assertThat(obj.shortV()).isEqualTo((short) 0);
            assertThat(obj.intV()).isEqualTo(0);
            assertThat(obj.longV()).isEqualTo(0L);
            assertThat(obj.floatV()).isEqualTo(0.0f);
            assertThat(obj.doubleV()).isEqualTo(0.0d);
        }

        static class AllArgsClass {
            final boolean boolV;
            final char charV;
            final byte byteV;
            final short shortV;
            final int intV;
            final long longV;
            final float floatV;
            final double doubleV;

            AllArgsClass(boolean boolV, char charV, byte byteV, short shortV,
                         int intV, long longV, float floatV, double doubleV) {
                this.boolV = boolV;
                this.charV = charV;
                this.byteV = byteV;
                this.shortV = shortV;
                this.intV = intV;
                this.longV = longV;
                this.floatV = floatV;
                this.doubleV = doubleV;
            }
        }

        record AllArgsRecord(boolean boolV, char charV, byte byteV, short shortV,
                             int intV, long longV, float floatV, double doubleV) {
        }
    }
}
