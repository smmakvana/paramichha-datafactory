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

/** Parsing annotation text such as @Min(18) into annotation instances. */
@DisplayName("StringAnnotationParser")
class StringAnnotationParserTest {

    @Nested
    @DisplayName("DefaultAnnotationParser")
    class ParsesAnnotationText {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("INSTANCE is non-null singleton")
            void instanceNonNull() {
                assertThat(StringAnnotationParser.INSTANCE).isNotNull();
            }

            @Test @DisplayName("new instance parses same as INSTANCE")
            void newInstanceWorks() {
                var parser = new StringAnnotationParser();
                assertThat(parser.parse("@NotNull")).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation — parse() branches")
        class ParseBranches {

            @Test @DisplayName("no-arg annotation without parens")
            void noArgNoParens() {
                var ann = StringAnnotationParser.INSTANCE.parse("@NotNull");
                assertThat(ann.annotationType()).isEqualTo(NotNull.class);
            }

            @Test @DisplayName("no-arg annotation with empty parens")
            void noArgEmptyParens() {
                var ann = StringAnnotationParser.INSTANCE.parse("@NotNull()");
                assertThat(ann.annotationType()).isEqualTo(NotNull.class);
            }

            @Test @DisplayName("one-arg positional form @Min(18)")
            void oneArgPositional() {
                var ann = (Min) StringAnnotationParser.INSTANCE.parse("@Min(18)");
                assertThat(ann.value()).isEqualTo(18L);
            }

            @Test @DisplayName("one-arg named form @Min(value=18)")
            void oneArgNamed() {
                var ann = (Min) StringAnnotationParser.INSTANCE.parse("@Min(value=18)");
                assertThat(ann.value()).isEqualTo(18L);
            }

            @Test @DisplayName("one-arg string @Pattern(regexp=\"^[A-Z]+$\")")
            void oneArgString() {
                var ann = (Pattern) StringAnnotationParser.INSTANCE.parse(
                        "@Pattern(regexp=\"^[A-Z]+$\")");
                assertThat(ann.regexp()).isEqualTo("^[A-Z]+$");
            }

            @Test @DisplayName("one-arg named string @DecimalMin(value=\"0.01\")")
            void oneArgNamedString() {
                var ann = (DecimalMin) StringAnnotationParser.INSTANCE.parse(
                        "@DecimalMin(value=\"0.01\")");
                assertThat(ann.value()).isEqualTo("0.01");
            }

            @Test @DisplayName("multi-arg @Size(min=2, max=50)")
            void multiArg() {
                var ann = (Size) StringAnnotationParser.INSTANCE.parse("@Size(min=2, max=50)");
                assertThat(ann.min()).isEqualTo(2);
                assertThat(ann.max()).isEqualTo(50);
            }

            @Test @DisplayName("unknown annotation throws AnnotationParseException")
            void unknownThrows() {
                assertThatThrownBy(() ->
                    StringAnnotationParser.INSTANCE.parse("@UnknownAnnotation")
                ).isInstanceOf(AnnotationParseException.class);
            }

            @Test @DisplayName("missing @ prefix throws AnnotationParseException")
            void missingAtThrows() {
                assertThatThrownBy(() ->
                    StringAnnotationParser.INSTANCE.parse("NotNull")
                ).isInstanceOf(AnnotationParseException.class);
            }

            @Test @DisplayName("malformed value throws AnnotationParseException")
            void malformedValueThrows() {
                assertThatThrownBy(() ->
                    StringAnnotationParser.INSTANCE.parse("@Min(notanumber)")
                ).isInstanceOf(AnnotationParseException.class);
            }

            @Test @DisplayName("getSupportedAnnotation returns null for unknown")
            void getSupportedAnnotationUnknown() {
                assertThat(StringAnnotationParser.INSTANCE.getAnnotation("Unknown"))
                        .isNull();
            }

            @Test @DisplayName("getAnnotationType returns null for unknown class")
            void getAnnotationTypeUnknown() {
                assertThat(StringAnnotationParser.INSTANCE.getArgumentType(Override.class))
                        .isNull();
            }
        }
    }
}
