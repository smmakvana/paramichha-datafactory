package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.builder.DefaultFieldBuilder;
import com.paramichha.datafactory.builder.FieldBuilderFactory;
import com.paramichha.datafactory.instantiation.AllArgsConstructorStrategy;
import com.paramichha.datafactory.instantiation.RecordStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collection;
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

    // ═══════════════════════════════════════════════════════════════════════
    // toSourceCode — all missing type branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toSourceCode — missing types")
    class ToSourceCode {

        @Test
        void short_has_no_suffix() {
            var codes = FieldBuilderFactory.create(Short.class).with("@Min(1)").with("@Max(10)").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
            codes.forEach(c -> assertThat(c).doesNotEndWith("L").doesNotEndWith("f"));
        }

        @Test
        void byte_has_no_suffix() {
            var codes = FieldBuilderFactory.create(Byte.class).with("@Min(1)").with("@Max(5)").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void double_returns_double_values() {
            var values = FieldBuilderFactory.create(Double.class).with("@Min(1)").with("@Max(100)").validList();
            assertThat(values).isNotEmpty();
            values.forEach(v -> assertThat(v).isInstanceOf(Double.class));
        }

        @Test
        void character_toString() {
            var codes = FieldBuilderFactory.create(Character.class).validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void date_source_code() {
            // Date field with @Past — validValues produces a past Date
            // toSourceCode hits the fallback value.toString()
            var codes = FieldBuilderFactory.create(Date.class).with("@Past").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void year_source_code() {
            var codes = FieldBuilderFactory.create(Year.class).with("@Past").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void yearMonth_source_code() {
            var codes = FieldBuilderFactory.create(YearMonth.class).with("@Past").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void bigInteger_source_code() {
            var codes = FieldBuilderFactory.create(BigInteger.class).with("@Min(1)").with("@Max(100)").validList().stream().map(Object::toString).toList();
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
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(BigInteger.class)).with("@Min(5)").constraintCases();
            var belowMin = cases.stream().filter(c -> c.constraint().equals("Min") && c.value() instanceof java.math.BigInteger).findFirst();
            assertThat(belowMin).isPresent();
        }

        @Test
        void aboveMax_returns_biginteger() {
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(BigInteger.class)).with("@Max(10)").constraintCases();
            var aboveMax = cases.stream().filter(c -> c.constraint().equals("Max") && c.value() instanceof java.math.BigInteger).findFirst();
            assertThat(aboveMax).isPresent();
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
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Date.class)).with("@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.constraint().equals("Past")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(Date.class);
        }

        @Test
        void year_past_notPast_is_year() {
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Year.class)).with("@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.constraint().equals("Past")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(Year.class);
            assertThat((Year) notPast.get().value()).isGreaterThan(Year.now());
        }

        @Test
        void yearMonth_past_notPast_is_yearmonth() {
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(YearMonth.class)).with("@Past").constraintCases();
            var notPast = cases.stream().filter(c -> c.constraint().equals("Past")).findFirst();
            assertThat(notPast).isPresent();
            assertThat(notPast.get().value()).isInstanceOf(YearMonth.class);
            assertThat((YearMonth) notPast.get().value()).isGreaterThan(YearMonth.now());
        }

        @Test
        void date_future_notFuture_is_date() {
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Date.class)).with("@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.constraint().equals("Future")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(Date.class);
        }

        @Test
        void year_future_notFuture_is_year() {
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Year.class)).with("@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.constraint().equals("Future")).findFirst();
            assertThat(notFuture).isPresent();
            assertThat(notFuture.get().value()).isInstanceOf(Year.class);
            assertThat((Year) notFuture.get().value()).isLessThan(Year.now());
        }

        @Test
        void yearMonth_future_notFuture_is_yearmonth() {
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(YearMonth.class)).with("@Future").constraintCases();
            var notFuture = cases.stream().filter(c -> c.constraint().equals("Future")).findFirst();
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
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Collection.class)).constraintCases();
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
            // @NegativeOrZero: max=0 -> aboveMax=1
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Integer.class)).with("@NegativeOrZero").constraintCases();
            assertThat(cases).anyMatch(c -> c.constraint().equals("Max")
                    && ((Integer)c.value()) == 1);
        }

        @Test
        void negative_only_positive_is_invalid() {
            // @Negative: max=-1 -> aboveMax=0
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Integer.class)).with("@Negative").constraintCases();
            assertThat(cases).anyMatch(c -> c.constraint().equals("Max")
                    && ((Integer)c.value()) == 0);
        }

        @Test
        void positive_not_allowed_negative_is_type_default() {
            // No min annotation -> negative type default appears
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Integer.class)).constraintCases();
            assertThat(cases).anyMatch(c -> !c.annotationDriven() && c.value() instanceof Integer i && i < 0);
        }

        @Test
        void with_min_no_negative_type_default() {
            // @Min(5) -> bounds.hasMin()=true -> no negative type default
            var cases = ((DefaultFieldBuilder<?>) FieldBuilderFactory.create(Integer.class).with("@Min(5)")).constraintCases();
            assertThat(cases).noneMatch(c -> !c.annotationDriven() && c.value() instanceof Integer i && i < 0);
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
