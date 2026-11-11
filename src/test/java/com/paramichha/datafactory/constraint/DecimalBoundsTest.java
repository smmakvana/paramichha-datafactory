package com.paramichha.datafactory.constraint;

import static com.paramichha.datafactory.constraint.ConstraintTestSupport.annotationsOf;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Decimal bounds: text that is not a number is ignored, and the convenience accessors truncate. */
@DisplayName("Decimal bounds")
class DecimalBoundsTest {

    record BadDecimals(
            @DecimalMin("abc") BigDecimal low,
            @DecimalMax("xyz") BigDecimal high) {}

    @Test
    @DisplayName("a @DecimalMin or @DecimalMax that is not a number gives no bound")
    void notANumberGivesNoBound() {
        assertThat(ClassAnnotationReader.INSTANCE.classifyQuantity(
                        annotationsOf(BadDecimals.class, "low").getFirst()))
                .isNull();
        assertThat(ClassAnnotationReader.INSTANCE.classifyQuantity(
                        annotationsOf(BadDecimals.class, "high").getFirst()))
                .isNull();
    }

    @Test
    @DisplayName("min() and max() truncate decimal bounds to whole numbers; unbounded has none")
    void convenienceAccessorsTruncate() {
        NumericBounds decimal = new NumericBounds.DecimalBounds(new BigDecimal("1.9"), new BigDecimal("8.2"));
        assertThat(decimal.min()).isEqualTo(1L);
        assertThat(decimal.max()).isEqualTo(8L);
        NumericBounds none = new NumericBounds.Unbounded();
        assertThat(none.min()).isNull();
        assertThat(none.max()).isNull();
        assertThat(none.isContradictory()).isFalse();
        assertThat(new NumericBounds.DecimalBounds(null, null).min()).isNull();
    }
}
