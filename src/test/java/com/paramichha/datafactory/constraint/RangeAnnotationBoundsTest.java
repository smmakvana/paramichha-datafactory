package com.paramichha.datafactory.constraint;

import static com.paramichha.datafactory.constraint.ConstraintTestSupport.annotationsOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import org.hibernate.validator.constraints.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A Hibernate @Range gives integer bounds, whether read from the annotation or from its text. */
@DisplayName("@Range bounds")
class RangeAnnotationBoundsTest {

    record Ranged(@Range(min = 1, max = 5) int level) {}

    @Test
    @DisplayName("@Range(min, max) gives integer bounds from the annotation and from its text")
    void rangeGivesIntegerBounds() {
        Annotation range = annotationsOf(Ranged.class, "level").getFirst();
        assertThat(ClassAnnotationReader.INSTANCE.classifyQuantity(range))
                .isEqualTo(new NumericBounds.IntegerBounds(1L, 5L));
        assertThat(TextAnnotationReader.INSTANCE.classifyQuantity("@Range(min=1, max=5)"))
                .isEqualTo(new NumericBounds.IntegerBounds(1L, 5L));
    }
}
