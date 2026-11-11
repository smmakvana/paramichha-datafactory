package com.paramichha.datafactory.generation;

import static com.paramichha.datafactory.generation.ShaperTestSupport.fieldOf;
import static com.paramichha.datafactory.generation.ShaperTestSupport.semantic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** UUID values: which fields the UUID shaper supports and what it shapes. */
@DisplayName("UuidTypeShaper")
class UuidTypeShaperTest {

    @Nested
    @DisplayName("UuidTypeShaper")
    class UuidValues {
        @Test
        void supportsUUID() {
            assertThat(UuidDataCreator.INSTANCE.supports(UUID.class)).isTrue();
        }

        @Test
        void supportsStringFalse() {
            assertThat(UuidDataCreator.INSTANCE.supports(String.class)).isFalse();
        }

        @Test
        void shapeReturnsUUID() {
            var f = fieldOf("id", UUID.class);
            assertThat(UuidDataCreator.INSTANCE.shape(f, semantic(), DataMachines.random()))
                    .isInstanceOf(UUID.class);
        }
    }
}
