package com.paramichha.datafactory.constraint;

import static com.paramichha.datafactory.constraint.ConstraintTestSupport.stdout;
import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.annotation.AnnotationProxyFactory;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The warnings the constraint checker prints, and how it looks annotations up by name. */
@DisplayName("ConstraintChecker")
class ConstraintCheckerTest {

    private static final List<Annotation> NOT_NULL = List.of(AnnotationProxyFactory.create(NotNull.class));

    @Test
    @DisplayName("a Short without @Min or @Max is reported with the range of its type")
    void aShortWithoutBoundsIsReportedWithItsTypeRange() {
        assertThat(stdout(() -> ConstraintChecker.warn("level", Short.class, NOT_NULL)))
                .contains("missing @Min/@Max")
                .contains("Short.MIN to Short.MAX");
    }

    @Test
    @DisplayName("an annotation is found by its simple name")
    void anAnnotationIsFoundByItsSimpleName() {
        assertThat(ConstraintChecker.hasAnyByName(NOT_NULL, "Size", "NotNull")).isTrue();
        assertThat(ConstraintChecker.hasAnyByName(NOT_NULL, "Size")).isFalse();
    }
}
