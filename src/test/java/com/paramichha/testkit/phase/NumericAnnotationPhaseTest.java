package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinByte;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinShort;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinInteger;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinLong;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinFloat;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinDouble;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinBigDecimal;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinBigInteger;
import com.paramichha.testkit.fixture.annotation.min.AnnotationMinMany;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxByte;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxShort;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxInteger;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxLong;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxFloat;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxDouble;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxBigDecimal;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxBigInteger;
import com.paramichha.testkit.fixture.annotation.max.AnnotationMaxMany;
import com.paramichha.testkit.fixture.annotation.decimalmin.AnnotationDecimalMinBigDecimal;
import com.paramichha.testkit.fixture.annotation.decimalmin.AnnotationDecimalMinDouble;
import com.paramichha.testkit.fixture.annotation.decimalmin.AnnotationDecimalMinString;
import com.paramichha.testkit.fixture.annotation.decimalmin.AnnotationDecimalMinMany;
import com.paramichha.testkit.fixture.annotation.decimalmax.AnnotationDecimalMaxBigDecimal;
import com.paramichha.testkit.fixture.annotation.decimalmax.AnnotationDecimalMaxDouble;
import com.paramichha.testkit.fixture.annotation.decimalmax.AnnotationDecimalMaxMany;
import com.paramichha.testkit.fixture.annotation.positive.AnnotationPositiveInteger;
import com.paramichha.testkit.fixture.annotation.positive.AnnotationPositiveLong;
import com.paramichha.testkit.fixture.annotation.positive.AnnotationPositiveFloat;
import com.paramichha.testkit.fixture.annotation.positive.AnnotationPositiveDouble;
import com.paramichha.testkit.fixture.annotation.positive.AnnotationPositiveBigDecimal;
import com.paramichha.testkit.fixture.annotation.positive.AnnotationPositiveBigInteger;
import com.paramichha.testkit.fixture.annotation.positive.AnnotationPositiveMany;
import com.paramichha.testkit.fixture.annotation.positiveorzero.AnnotationPositiveOrZeroInteger;
import com.paramichha.testkit.fixture.annotation.positiveorzero.AnnotationPositiveOrZeroLong;
import com.paramichha.testkit.fixture.annotation.positiveorzero.AnnotationPositiveOrZeroBigDecimal;
import com.paramichha.testkit.fixture.annotation.positiveorzero.AnnotationPositiveOrZeroMany;
import com.paramichha.testkit.fixture.annotation.negative.AnnotationNegativeInteger;
import com.paramichha.testkit.fixture.annotation.negative.AnnotationNegativeLong;
import com.paramichha.testkit.fixture.annotation.negative.AnnotationNegativeBigDecimal;
import com.paramichha.testkit.fixture.annotation.negative.AnnotationNegativeMany;
import com.paramichha.testkit.fixture.annotation.negativeorzero.AnnotationNegativeOrZeroInteger;
import com.paramichha.testkit.fixture.annotation.negativeorzero.AnnotationNegativeOrZeroLong;
import com.paramichha.testkit.fixture.annotation.negativeorzero.AnnotationNegativeOrZeroMany;
import com.paramichha.testkit.fixture.annotation.digits.AnnotationDigitsBigDecimal;
import com.paramichha.testkit.fixture.annotation.digits.AnnotationDigitsString;
import com.paramichha.testkit.fixture.annotation.digits.AnnotationDigitsMany;

@DisplayName("Phase 5 — Numeric annotations")
class NumericAnnotationPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            AnnotationMinByte.class,
            AnnotationMinShort.class,
            AnnotationMinInteger.class,
            AnnotationMinLong.class,
            AnnotationMinFloat.class,
            AnnotationMinDouble.class,
            AnnotationMinBigDecimal.class,
            AnnotationMinBigInteger.class,
            AnnotationMinMany.class,
            AnnotationMaxByte.class,
            AnnotationMaxShort.class,
            AnnotationMaxInteger.class,
            AnnotationMaxLong.class,
            AnnotationMaxFloat.class,
            AnnotationMaxDouble.class,
            AnnotationMaxBigDecimal.class,
            AnnotationMaxBigInteger.class,
            AnnotationMaxMany.class,
            AnnotationDecimalMinBigDecimal.class,
            AnnotationDecimalMinDouble.class,
            AnnotationDecimalMinString.class,
            AnnotationDecimalMinMany.class,
            AnnotationDecimalMaxBigDecimal.class,
            AnnotationDecimalMaxDouble.class,
            AnnotationDecimalMaxMany.class,
            AnnotationPositiveInteger.class,
            AnnotationPositiveLong.class,
            AnnotationPositiveFloat.class,
            AnnotationPositiveDouble.class,
            AnnotationPositiveBigDecimal.class,
            AnnotationPositiveBigInteger.class,
            AnnotationPositiveMany.class,
            AnnotationPositiveOrZeroInteger.class,
            AnnotationPositiveOrZeroLong.class,
            AnnotationPositiveOrZeroBigDecimal.class,
            AnnotationPositiveOrZeroMany.class,
            AnnotationNegativeInteger.class,
            AnnotationNegativeLong.class,
            AnnotationNegativeBigDecimal.class,
            AnnotationNegativeMany.class,
            AnnotationNegativeOrZeroInteger.class,
            AnnotationNegativeOrZeroLong.class,
            AnnotationNegativeOrZeroMany.class,
            AnnotationDigitsBigDecimal.class,
            AnnotationDigitsString.class,
            AnnotationDigitsMany.class
        ).writeTo(tmp);
        assertThat(written).hasSize(46);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(AnnotationMinByte.class).generate().asString(), "AnnotationMinByte");
        assertStructure(TestKit.of(AnnotationMinShort.class).generate().asString(), "AnnotationMinShort");
        assertStructure(TestKit.of(AnnotationMinInteger.class).generate().asString(), "AnnotationMinInteger");
        assertStructure(TestKit.of(AnnotationMinLong.class).generate().asString(), "AnnotationMinLong");
        assertStructure(TestKit.of(AnnotationMinFloat.class).generate().asString(), "AnnotationMinFloat");
        assertStructure(TestKit.of(AnnotationMinDouble.class).generate().asString(), "AnnotationMinDouble");
        assertStructure(TestKit.of(AnnotationMinBigDecimal.class).generate().asString(), "AnnotationMinBigDecimal");
        assertStructure(TestKit.of(AnnotationMinBigInteger.class).generate().asString(), "AnnotationMinBigInteger");
        assertStructure(TestKit.of(AnnotationMinMany.class).generate().asString(), "AnnotationMinMany");
        assertStructure(TestKit.of(AnnotationMaxByte.class).generate().asString(), "AnnotationMaxByte");
        assertStructure(TestKit.of(AnnotationMaxShort.class).generate().asString(), "AnnotationMaxShort");
        assertStructure(TestKit.of(AnnotationMaxInteger.class).generate().asString(), "AnnotationMaxInteger");
        assertStructure(TestKit.of(AnnotationMaxLong.class).generate().asString(), "AnnotationMaxLong");
        assertStructure(TestKit.of(AnnotationMaxFloat.class).generate().asString(), "AnnotationMaxFloat");
        assertStructure(TestKit.of(AnnotationMaxDouble.class).generate().asString(), "AnnotationMaxDouble");
        assertStructure(TestKit.of(AnnotationMaxBigDecimal.class).generate().asString(), "AnnotationMaxBigDecimal");
        assertStructure(TestKit.of(AnnotationMaxBigInteger.class).generate().asString(), "AnnotationMaxBigInteger");
        assertStructure(TestKit.of(AnnotationMaxMany.class).generate().asString(), "AnnotationMaxMany");
        assertStructure(TestKit.of(AnnotationDecimalMinBigDecimal.class).generate().asString(), "AnnotationDecimalMinBigDecimal");
        assertStructure(TestKit.of(AnnotationDecimalMinDouble.class).generate().asString(), "AnnotationDecimalMinDouble");
        assertStructure(TestKit.of(AnnotationDecimalMinString.class).generate().asString(), "AnnotationDecimalMinString");
        assertStructure(TestKit.of(AnnotationDecimalMinMany.class).generate().asString(), "AnnotationDecimalMinMany");
        assertStructure(TestKit.of(AnnotationDecimalMaxBigDecimal.class).generate().asString(), "AnnotationDecimalMaxBigDecimal");
        assertStructure(TestKit.of(AnnotationDecimalMaxDouble.class).generate().asString(), "AnnotationDecimalMaxDouble");
        assertStructure(TestKit.of(AnnotationDecimalMaxMany.class).generate().asString(), "AnnotationDecimalMaxMany");
        assertStructure(TestKit.of(AnnotationPositiveInteger.class).generate().asString(), "AnnotationPositiveInteger");
        assertStructure(TestKit.of(AnnotationPositiveLong.class).generate().asString(), "AnnotationPositiveLong");
        assertStructure(TestKit.of(AnnotationPositiveFloat.class).generate().asString(), "AnnotationPositiveFloat");
        assertStructure(TestKit.of(AnnotationPositiveDouble.class).generate().asString(), "AnnotationPositiveDouble");
        assertStructure(TestKit.of(AnnotationPositiveBigDecimal.class).generate().asString(), "AnnotationPositiveBigDecimal");
        assertStructure(TestKit.of(AnnotationPositiveBigInteger.class).generate().asString(), "AnnotationPositiveBigInteger");
        assertStructure(TestKit.of(AnnotationPositiveMany.class).generate().asString(), "AnnotationPositiveMany");
        assertStructure(TestKit.of(AnnotationPositiveOrZeroInteger.class).generate().asString(), "AnnotationPositiveOrZeroInteger");
        assertStructure(TestKit.of(AnnotationPositiveOrZeroLong.class).generate().asString(), "AnnotationPositiveOrZeroLong");
        assertStructure(TestKit.of(AnnotationPositiveOrZeroBigDecimal.class).generate().asString(), "AnnotationPositiveOrZeroBigDecimal");
        assertStructure(TestKit.of(AnnotationPositiveOrZeroMany.class).generate().asString(), "AnnotationPositiveOrZeroMany");
        assertStructure(TestKit.of(AnnotationNegativeInteger.class).generate().asString(), "AnnotationNegativeInteger");
        assertStructure(TestKit.of(AnnotationNegativeLong.class).generate().asString(), "AnnotationNegativeLong");
        assertStructure(TestKit.of(AnnotationNegativeBigDecimal.class).generate().asString(), "AnnotationNegativeBigDecimal");
        assertStructure(TestKit.of(AnnotationNegativeMany.class).generate().asString(), "AnnotationNegativeMany");
        assertStructure(TestKit.of(AnnotationNegativeOrZeroInteger.class).generate().asString(), "AnnotationNegativeOrZeroInteger");
        assertStructure(TestKit.of(AnnotationNegativeOrZeroLong.class).generate().asString(), "AnnotationNegativeOrZeroLong");
        assertStructure(TestKit.of(AnnotationNegativeOrZeroMany.class).generate().asString(), "AnnotationNegativeOrZeroMany");
        assertStructure(TestKit.of(AnnotationDigitsBigDecimal.class).generate().asString(), "AnnotationDigitsBigDecimal");
        assertStructure(TestKit.of(AnnotationDigitsString.class).generate().asString(), "AnnotationDigitsString");
        assertStructure(TestKit.of(AnnotationDigitsMany.class).generate().asString(), "AnnotationDigitsMany");
    }

    private static void assertStructure(String src, String className) {
        // Accept either a full generated test or a placeholder comment file
        boolean isFull = src.contains("class " + className + "Test")
                && src.contains("class SmokeTest");
        boolean isPlaceholder = src.contains("TODO: " + className + "Test");
        assertThat(isFull || isPlaceholder)
                .as("Expected full test or placeholder for %s", className)
                .isTrue();
    }
}
