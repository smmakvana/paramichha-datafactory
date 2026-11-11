package com.paramichha.datafactory.generation;

import java.time.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static com.paramichha.datafactory.generation.ShaperTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Boolean values: which fields the boolean shaper supports and what it shapes. */
@DisplayName("BooleanTypeShaper")
class BooleanTypeShaperTest {

    @Nested
    @DisplayName("BooleanTypeShaper")
    class BooleanValues {
        @Test
        void supportsBoolean() {
            assertThat(BooleanDataCreator.INSTANCE.supports(Boolean.class)).isTrue();
        }

        @Test
        void supportsBooleanPrimitive() {
            assertThat(BooleanDataCreator.INSTANCE.supports(boolean.class)).isTrue();
        }

        @Test
        void supportsStringFalse() {
            assertThat(BooleanDataCreator.INSTANCE.supports(String.class)).isFalse();
        }

        @Test
        void shapeTrue() {
            assertThat(BooleanDataCreator.INSTANCE.shape(fieldOf("f", Boolean.class), BoundaryTarget.trueTarget(), DataMachines.random())).isEqualTo(true);
        }

        @Test
        void shapeFalse() {
            assertThat(BooleanDataCreator.INSTANCE.shape(fieldOf("f", Boolean.class), BoundaryTarget.falseTarget(), DataMachines.random())).isEqualTo(false);
        }
    }
}
