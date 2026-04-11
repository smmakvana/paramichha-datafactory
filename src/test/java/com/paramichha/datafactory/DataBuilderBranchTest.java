package com.paramichha.datafactory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Targets the 2 missing branches in DefaultDataBuilder:
 * isBooleanType — boolean.class (primitive) arm
 * validAll — boolean primitive field is excluded
 */
@DisplayName("DefaultDataBuilder — missing branch coverage")
class DataBuilderBranchTest {

    @Value
    @Builder
    static class PrimitiveBoolRequest {
        boolean active;           // primitive boolean — hits isBooleanType(boolean.class)
        @Min(1)
        @Max(10)
        int count;
    }

    @Value
    @Builder
    static class WrapperBoolRequest {
        Boolean active;           // wrapper boolean — hits isBooleanType(Boolean.class)
        @Min(1)
        @Max(10)
        int count;
    }

    @Nested
    @DisplayName("isBooleanType — boolean.class primitive arm")
    class BooleanPrimitive {

        @Test
        void validAll_excludes_primitive_boolean() {
            Map<String, List<PrimitiveBoolRequest>> all =
                    DataFactory.of(PrimitiveBoolRequest.class).validAll();
            // count has bounds -> included; active is boolean.class -> excluded
            assertThat(all).containsKey("count");
            assertThat(all).doesNotContainKey("active");
        }

        @Test
        void valid_with_primitive_boolean() {
            var req = DataFactory.of(PrimitiveBoolRequest.class).valid();
            assertThat(req).isNotNull();
        }
    }

    @Nested
    @DisplayName("isBooleanType — Boolean.class wrapper arm")
    class BooleanWrapper {

        @Test
        void validAll_excludes_wrapper_boolean() {
            Map<String, List<WrapperBoolRequest>> all =
                    DataFactory.of(WrapperBoolRequest.class).validAll();
            assertThat(all).containsKey("count");
            assertThat(all).doesNotContainKey("active");
        }
    }
}
