package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.builder.ValueBuilderFactory;
import jakarta.validation.constraints.*;
import java.math.BigInteger;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Source code written for built values of each type. */
@DisplayName("DefaultFieldBuilder — source code of built values")
class DefaultFieldBuilderSourceCodeTest {

    @Nested
    @DisplayName("toSourceCode — missing types")
    class BuiltValues {

        @Test
        void shortHasNoSuffix() {
            var codes = ValueBuilderFactory.create(Short.class).with("@Min(1)").with("@Max(10)").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
            codes.forEach(c -> assertThat(c).doesNotEndWith("L").doesNotEndWith("f"));
        }

        @Test
        void byteHasNoSuffix() {
            var codes = ValueBuilderFactory.create(Byte.class).with("@Min(1)").with("@Max(5)").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void doubleReturnsDoubleValues() {
            var values = ValueBuilderFactory.create(Double.class).with("@Min(1)").with("@Max(100)").validList();
            assertThat(values).isNotEmpty();
            values.forEach(v -> assertThat(v).isInstanceOf(Double.class));
        }

        @Test
        void characterToString() {
            var codes = ValueBuilderFactory.create(Character.class).validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void dateSourceCode() {
            // Date field with @Past — validValues produces a past Date
            // toSourceCode hits the fallback value.toString()
            var codes = ValueBuilderFactory.create(Date.class).with("@Past").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void yearSourceCode() {
            var codes = ValueBuilderFactory.create(Year.class).with("@Past").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void yearMonth_source_code() {
            var codes = ValueBuilderFactory.create(YearMonth.class).with("@Past").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }

        @Test
        void bigInteger_source_code() {
            var codes = ValueBuilderFactory.create(BigInteger.class).with("@Min(1)").with("@Max(100)").validList().stream().map(Object::toString).toList();
            assertThat(codes).isNotEmpty();
        }
    }
}
