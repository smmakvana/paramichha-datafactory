package com.paramichha.datafactory.constraint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paramichha.datafactory.AnnotationParseException;
import com.paramichha.datafactory.builder.StringAnnotationParser;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Coercing and unquoting the single value of an annotation argument. */
@DisplayName("SingleArgumentHandler")
class SingleArgumentHandlerTest {

    @Nested
    @DisplayName("SingleArgumentHandler — coerce and stripQuotes")
    class ValueCoercion {

        @Nested
        @DisplayName("Validation — value coercion")
        class Coercion {

            @Test
            @DisplayName("long value coerced for @Min")
            void longCoerced() {
                var ann = (Min) StringAnnotationParser.INSTANCE.parse("@Min(100)");
                assertThat(ann.value()).isEqualTo(100L);
            }

            @Test
            @DisplayName("String value kept for @Pattern")
            void stringKept() {
                var ann = (Pattern) StringAnnotationParser.INSTANCE.parse("@Pattern(regexp=\"[A-Z]+\")");
                assertThat(ann.regexp()).isEqualTo("[A-Z]+");
            }

            @Test
            @DisplayName("single-quoted string stripped")
            void singleQuoteStripped() {
                var ann = (DecimalMax) StringAnnotationParser.INSTANCE.parse("@DecimalMax(value='9.99')");
                assertThat(ann.value()).isEqualTo("9.99");
            }

            @Test
            @DisplayName("unmatched quotes not stripped")
            void unmatchedQuotesKept() {
                assertThatThrownBy(() -> StringAnnotationParser.INSTANCE.parse("@Min(\"abc\")"))
                        .isInstanceOf(AnnotationParseException.class);
            }

            @Test
            @DisplayName("named form with spaces: value = 42")
            void namedWithSpaces() {
                var ann = (Max) StringAnnotationParser.INSTANCE.parse("@Max(value = 42)");
                assertThat(ann.value()).isEqualTo(42L);
            }

            @Test
            @DisplayName("empty raw string throws")
            void emptyRawThrows() {
                assertThatThrownBy(() -> StringAnnotationParser.INSTANCE.parse("@Min()"))
                        .isInstanceOf(AnnotationParseException.class);
            }
        }
    }
}
