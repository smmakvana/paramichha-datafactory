package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static com.paramichha.datafactory.generation.ShaperTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/** The registry of type shapers: which shaper handles which type, including collections. */
@DisplayName("TypeShaperRegistry")
class TypeShaperRegistryTest {

    @Nested
    @DisplayName("TypeShaperRegistry — collection support")
    class CollectionSupport {

        @Test
        void listType_isSupported() {
            // List used to have no creator; CollectionDataCreator now handles it.
            // Truly unsupported types throw DataFactoryException ("no silent null").
            var f = AnnotationResolver.analyzeFromStrings("tags", "List", List.of());
            Object result = DataGenerator.shape(f, BoundaryTarget.semantic(), DataMachines.random());
            assertThat(result).isNotNull();
        }
    }
}
