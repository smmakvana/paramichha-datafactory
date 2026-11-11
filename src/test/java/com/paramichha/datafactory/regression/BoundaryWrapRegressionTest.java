package com.paramichha.datafactory.regression;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.ConstraintCase;
import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.FieldCases;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Regression: the value just outside a bound was cast to the field type without checking it fits.
 * {@code @Max(127) byte} produced {@code 127 + 1 = -128}, an invalid value that looks like a valid minimum.
 */
class BoundaryWrapRegressionTest {

    record ByteMax(@Max(127) byte level) {}

    record ByteMin(@Min(-128) byte level) {}

    record ShortMax(@Max(32767) short level) {}

    record IntMax(@Max(10) int level) {}

    private static List<ConstraintCase> cases(Class<?> type, String field) {
        Map<String, com.paramichha.datafactory.DataFactoryResult<FieldCases>> fields = DataFactory.fields(type);
        return fields.get(field).get().invalidCases();
    }

    @Test
    void byteMaxAtTypeLimitHasNoWrappingMaxCase() {
        assertThat(cases(ByteMax.class, "level"))
                .extracting(ConstraintCase::constraint)
                .doesNotContain("Max");
        assertThat(cases(ByteMax.class, "level"))
                .extracting(ConstraintCase::value)
                .doesNotContain((byte) -128);
    }

    @Test
    void byteMinAtTypeLimitHasNoWrappingMinCase() {
        assertThat(cases(ByteMin.class, "level"))
                .extracting(ConstraintCase::constraint)
                .doesNotContain("Min");
    }

    @Test
    void shortMaxAtTypeLimitHasNoWrappingMaxCase() {
        assertThat(cases(ShortMax.class, "level"))
                .extracting(ConstraintCase::constraint)
                .doesNotContain("Max");
    }

    @Test
    void ordinaryBoundsStillProduceTheMaxCase() {
        assertThat(cases(IntMax.class, "level"))
                .extracting(ConstraintCase::constraint)
                .contains("Max");
        assertThat(cases(IntMax.class, "level"))
                .extracting(ConstraintCase::value)
                .contains(11);
    }
}
