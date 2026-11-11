package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.builder.FieldValueBuilder;
import com.paramichha.datafactory.builder.ValueBuilderFactory;
import jakarta.validation.constraints.*;
import java.math.BigInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Numeric fallbacks: BigInteger casting and whether a negative value is allowed. */
@DisplayName("DefaultFieldBuilder — numeric fallbacks")
class NumericFallbackTest {

    @Nested
    @DisplayName("castNumeric — BigInteger")
    class BigIntegerCasting {

        @Test
        void belowMin_returns_biginteger() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(BigInteger.class)).with("@Min(5)").constraintCases();
            var belowMin = cases.stream().filter(c -> c.constraint().equals("Min") && c.value() instanceof java.math.BigInteger).findFirst();
            assertThat(belowMin).isPresent();
        }

        @Test
        void aboveMax_returns_biginteger() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(BigInteger.class)).with("@Max(10)").constraintCases();
            var aboveMax = cases.stream().filter(c -> c.constraint().equals("Max") && c.value() instanceof java.math.BigInteger).findFirst();
            assertThat(aboveMax).isPresent();
        }
    }

    @Nested
    @DisplayName("isNegativeAllowed — max <= 0 means negative is valid")
    class NegativeAllowed {

        @Test
        void negativeAllowedPositiveIsInvalid() {
            // @NegativeOrZero: max=0 -> aboveMax=1
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Integer.class)).with("@NegativeOrZero").constraintCases();
            assertThat(cases).anyMatch(c -> c.constraint().equals("Max")
                    && ((Integer)c.value()) == 1);
        }

        @Test
        void negativeOnlyPositiveIsInvalid() {
            // @Negative: max=-1 -> aboveMax=0
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Integer.class)).with("@Negative").constraintCases();
            assertThat(cases).anyMatch(c -> c.constraint().equals("Max")
                    && ((Integer)c.value()) == 0);
        }

        @Test
        void positiveNotAllowedNegativeIsTypeDefault() {
            // No min annotation -> negative type default appears
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Integer.class)).constraintCases();
            assertThat(cases).anyMatch(c -> !c.annotationDriven() && c.value() instanceof Integer i && i < 0);
        }

        @Test
        void withMinNoNegativeTypeDefault() {
            // @Min(5) -> bounds.hasMin()=true -> no negative type default
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Integer.class).with("@Min(5)")).constraintCases();
            assertThat(cases).noneMatch(c -> !c.annotationDriven() && c.value() instanceof Integer i && i < 0);
        }
    }
}
