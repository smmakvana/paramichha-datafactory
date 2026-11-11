package com.paramichha.datafactory.generation;

import static com.paramichha.datafactory.generation.ShaperTestSupport.semantic;
import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.constraint.FieldConstraints;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Enum values: which fields the enum shaper supports and what it shapes. */
@DisplayName("EnumTypeShaper")
class EnumTypeShaperTest {

    @Nested
    @DisplayName("EnumTypeShaper")
    class EnumValues {
        @Test
        void supportsEnum() {
            assertThat(EnumDataCreator.INSTANCE.supports(Status.class)).isTrue();
        }

        @Test
        void supportsStringFalse() {
            assertThat(EnumDataCreator.INSTANCE.supports(String.class)).isFalse();
        }

        @Test
        void shapeReturnsAllConstants() {
            // Build an FieldConstraints with fieldType = Status.class directly via the builder
            var analyzed = FieldConstraints.builder("status", Status.class).build();
            @SuppressWarnings("unchecked")
            var result = (List<Object>) EnumDataCreator.INSTANCE.shape(analyzed, semantic(), DataMachines.random());
            assertThat(result).containsExactlyInAnyOrder(Status.ACTIVE, Status.INACTIVE);
        }

        enum Status {
            ACTIVE,
            INACTIVE
        }
    }
}
