package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.annotation.AnnotationProxyFactory;
import jakarta.validation.Valid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.paramichha.datafactory.constraint.ConstraintTestSupport.stdout;
import static org.assertj.core.api.Assertions.assertThat;

/** @Valid without @NotNull is reported, because null skips the cascade. */
@DisplayName("@Valid without @NotNull")
class ValidWithoutNotNullWarningTest {

    record Nested(String name) {}

    @Test
    @DisplayName("is reported by the constraint checker")
    void isReported() {
        Valid valid = AnnotationProxyFactory.create(Valid.class);
        assertThat(stdout(() -> ConstraintChecker.warn("child", Nested.class, List.of(valid))))
                .contains("@Valid without @NotNull");
    }
}
