package com.paramichha.datafactory;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boolean fields in validList() — primitive and wrapper, @AssertTrue and @AssertFalse.
 * All four combinations must be excluded from the boundary map.
 */
@DisplayName("DataBuilder — boolean field exclusion from validList()")
class DataBuilderBooleanTest {

    @Value @Builder static class PrimitiveBool { boolean active; @Min(1) @Max(10) int count; }
    @Value @Builder static class WrapperBool   { Boolean active; @Min(1) @Max(10) int count; }
    @Value @Builder static class AssertTrueReq { @AssertTrue Boolean active; @Min(1) @Max(10) Integer count; }
    @Value @Builder static class AssertFalseReq{ @AssertFalse Boolean deleted; @Min(1) @Max(10) Integer count; }

    @ParameterizedTest(name = "{0}")
    @MethodSource("booleanFieldCases")
    @DisplayName("boolean field excluded, numeric field included")
    void boolean_excluded_numeric_included(String label, Class<?> type, String boolField, String numericField) {
        Map<String, ?> all = DataFactory.of(type).validMap();
        assertThat(all).containsKey(numericField);
        assertThat(all).doesNotContainKey(boolField);
    }

    static Stream<Arguments> booleanFieldCases() {
        return Stream.of(
            Arguments.of("boolean primitive",  PrimitiveBool.class,  "active",  "count"),
            Arguments.of("Boolean wrapper",    WrapperBool.class,    "active",  "count"),
            Arguments.of("@AssertTrue Boolean",AssertTrueReq.class,  "active",  "count"),
            Arguments.of("@AssertFalse Boolean",AssertFalseReq.class,"deleted", "count")
        );
    }

    @Nested
    @DisplayName("valid() still assembles correctly")
    class ValidAssembly {
        @Test
        void primitive_boolean_valid_assembles() {
            assertThat(DataFactory.of(PrimitiveBool.class).valid()).isNotNull();
        }

        @Test
        void wrapper_boolean_valid_assembles() {
            assertThat(DataFactory.of(WrapperBool.class).valid()).isNotNull();
        }
    }
}
