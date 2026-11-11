package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.AnnotationParseException;
import com.paramichha.datafactory.builder.StringAnnotationParser;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Annotations that take no arguments reject any attribute. */
@DisplayName("NoArgHandler")
class NoArgHandlerTest {

    @Nested
    @DisplayName("NoArgHandler — rejects attributes")
    class RejectsAttributes {

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("no attributes parses correctly")
            void noAttributesOk() {
                assertThat(StringAnnotationParser.INSTANCE.parse("@NotNull")).isNotNull();
            }

            @Test @DisplayName("attributes given throws AnnotationParseException")
            void attributesThrows() {
                assertThatThrownBy(() ->
                    StringAnnotationParser.INSTANCE.parse("@NotNull(someValue)")
                ).isInstanceOf(AnnotationParseException.class);
            }
        }
    }
}
