package com.paramichha.datafactory;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the 2 remaining missing branches in DefaultDataBuilder:
 * isBooleanType — @AssertTrue Boolean wrapper excluded from validAll()
 * isBooleanType — @AssertFalse Boolean wrapper excluded from validAll()
 */
@DisplayName("DefaultDataBuilder — remaining branch coverage")
class DataFactoryGapTest {

    @Test
    void validAll_excludes_assertTrue_boolean_wrapper() {
        Map<String, List<AssertTrueRequest>> all =
                DataFactory.of(AssertTrueRequest.class).validAll();
        assertThat(all).containsKey("count");
        assertThat(all).doesNotContainKey("active");
    }

    @Test
    void validAll_excludes_assertFalse_boolean_wrapper() {
        Map<String, List<AssertFalseRequest>> all =
                DataFactory.of(AssertFalseRequest.class).validAll();
        assertThat(all).containsKey("count");
        assertThat(all).doesNotContainKey("deleted");
    }

    @Value
    @Builder
    static class AssertTrueRequest {
        @AssertTrue
        Boolean active;
        @Min(1)
        @Max(10)
        Integer count;
    }

    @Value
    @Builder
    static class AssertFalseRequest {
        @AssertFalse
        Boolean deleted;
        @Min(1)
        @Max(10)
        Integer count;
    }
}
