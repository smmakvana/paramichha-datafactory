package com.paramichha.datafactory.constraint;

import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.paramichha.datafactory.constraint.ConstraintTestSupport.annotationsOf;
import static com.paramichha.datafactory.constraint.ConstraintTestSupport.stderr;
import static org.assertj.core.api.Assertions.assertThat;

/** Two format annotations on one field: the first wins and the conflict is reported on stderr. */
@DisplayName("Conflicting format annotations")
class FormatConflictTest {

    record TwoFormats(@Email @URL String contact) {}

    @Test
    @DisplayName("the first format annotation wins and the conflict is reported")
    void firstFormatWinsAndConflictIsReported() {
        String err = stderr(() -> assertThat(AnnotationResolver.analyze(
                "contact", String.class, annotationsOf(TwoFormats.class, "contact")).format())
                .isEqualTo(FormatType.EMAIL));
        assertThat(err).contains("conflicting format annotations: EMAIL and URL");
    }
}
