package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import com.paramichha.datafactory.constraint.FieldConstraints;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static com.paramichha.datafactory.generation.ShaperTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/** The value generator: null targets, unknown types and enum expansion. */
@DisplayName("ValueGenerator")
class ValueGeneratorTest {

    @Nested
    @DisplayName("ValueGenerator")
    class ValueGenerator {

        @Test
        void nullTarget_returnsNull() {
            var f = fieldOf("x", String.class);
            assertThat(DataGenerator.shape(f, BoundaryTarget.nullTarget(), DataMachines.random())).isNull();
        }

        @Test
        void shapeAll_enumExpansion() {
            // Enum field → shape() returns List of all constants → shapeAll expands each as separate entry
            enum Color {RED, GREEN, BLUE}
            var analyzed = FieldConstraints.builder("color", Color.class).build();
            var targets = BoundaryPlanner.plan(analyzed);
            var result = DataGenerator.shapeAll(analyzed, targets, DataMachines.random());
            assertThat(result).containsExactlyInAnyOrder(Color.RED, Color.GREEN, Color.BLUE);
        }

        @Test
        void shapeAll_listIsFlattenedAndNullFree() {
            // List is handled by CollectionDataCreator; shapeAll flattens the
            // generated list into element values and strips nulls.
            var f = AnnotationResolver.analyzeFromStrings("tags", "List", List.of());
            var targets = List.of(BoundaryTarget.semantic());
            var result = DataGenerator.shapeAll(f, targets, DataMachines.random());
            assertThat(result).isNotEmpty().doesNotContainNull();
        }

        @Test
        void shapeAll_singleValidString() {
            var f = AnnotationResolver.analyzeFromStrings("name", "String", List.of("@NotBlank"));
            var result = DataGenerator.shapeAll(f, BoundaryPlanner.plan(f), DataMachines.random());
            assertThat(result).isNotEmpty();
            result.forEach(v -> assertThat(v).isInstanceOf(String.class));
        }
    }
}
