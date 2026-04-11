package com.paramichha.testkit.annotation;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnnotationParserTest {

    private AnnotationParser parser;

    @BeforeEach
    void setup() {
        parser = new DefaultAnnotationParser();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Global invalid
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class SmokeTest {

        @Nested
        @DisplayName("Valid annotation — returns correct Annotation instance")
        class ValidScenario {

            @ParameterizedTest(name = "[{index}] \"{0}\"")
            @MethodSource("valid")
            void parsesSuccessfully(String input) throws AnnotationParseException {
                Annotation result = parser.parse(input);
                assertThat(result).isNotNull();
                assertThat(result.annotationType()).isNotNull();
                assertThat(result.hashCode()).isNotNull();
            }

            static Stream<String> valid() {
                return Stream.of(
                        "@NotNull",
                        "@NotBlank",
                        "@NotEmpty",
                        "@Null",
                        "@Valid",

                        "@Email",
                        "@URL",
                        "@CreditCardNumber",
                        "@ISBN",
                        "@EAN",
                        "@UUID",

                        "@Pattern(regexp=\"^[A-Z]+$\")",
                        "@Pattern(regexp=\"^[A-Z]{2}\\\\d{4}$\")",

                        "@Min(18)",
                        "@Min(0)",
                        "@Min(-5)",

                        "@Max(100)",
                        "@Max(0)",

                        "@DecimalMin(10.5)",
                        "@DecimalMax(999.99)",

                        "@Positive",
                        "@PositiveOrZero",
                        "@Negative",
                        "@NegativeOrZero",

                        "@Size(min=1, max=10)",
                        "@Size(min=0, max=2147483647)",

                        "@Length(min=3, max=20)",

                        "@Digits(integer=8, fraction=2)",

                        "@Past",
                        "@PastOrPresent",
                        "@Future",
                        "@FutureOrPresent",

                        "@AssertTrue",
                        "@AssertFalse"
                );
            }
        }

        @Nested
        @DisplayName("Invalid annotation — throws AnnotationParseException")
        class InValidScenario {

            @ParameterizedTest(name = "[{index}] \"{0}\"")
            @MethodSource("invalid")
            void throwsOnMalformed(String input) {
                assertThatThrownBy(() -> parser.parse(input))
                        .isInstanceOf(AnnotationParseException.class);
            }

            static Stream<String> invalid() {
                return Stream.of(
                        null, "",
                        "   ",

                        "@",
                        "@(",
                        "@((",
                        "@)",
                        "@))",
                        "@X(",
                        "@X((",
                        "@X)",
                        "@X))",

                        "@)(",
                        "@X)(",

                        "NotNull",
                        "Min(10)",
                        "@ Unknown",

                        "@NotNull(",
                        "@Min(",
                        "@Size(",

                        "@Unknown",
                        "@Unknown(10)",
                        "@MyCustom",
                        "@MyCustom(abc)",

                        "@Min()",
                        "@Max()",
                        "@Digits()",

                        "@Min(abc)",
                        "@Max(xyz)",
                        "@Min(10",
                        "@Min(10))",

                        "@Digits(integer=abc, fraction=2)",
                        "@Digits(integer=8, fraction=xyz)",

                        "@Digits(integer=8, fraction=2))",
                        "@Digits(integer=8, fraction=2",

                        "@Size(min=1, max=10))",
                        "@Size(min=abc, max=10)",

                        "@Size(min=1, max=xyz)",
                        "@Size(min=, max=10)",

                        "@Size(min=1, max=)",
                        "@Size(min=1, max=10",

                        "@Pattern(",
                        "@Pattern(regexp=)",
                        "@Pattern(regexp=\""
                );
            }

        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AnnotationParseException — both constructors
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AnnotationParseException")
    class ClazzParserExceptionTest {

        @org.junit.jupiter.api.Test
        void message_only_constructor() {
            var ex = new AnnotationParseException("bad input");
            assertThat(ex.getMessage()).isEqualTo("bad input");
            assertThat(ex.getCause()).isNull();
        }

        @org.junit.jupiter.api.Test
        void message_and_cause_constructor() {
            var cause = new RuntimeException("root cause");
            var ex = new AnnotationParseException("bad input", cause);
            assertThat(ex.getMessage()).isEqualTo("bad input");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AnnotationProxyFactory — equals branch
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AnnotationProxyFactory")
    class AnnotationProxyFactoryTest {

        @org.junit.jupiter.api.Test
        void same_proxy_equals_itself() throws AnnotationParseException {
            java.lang.annotation.Annotation a = parser.parse("@NotNull");
            assertThat(a.equals(a)).isTrue();
        }

        @org.junit.jupiter.api.Test
        void different_proxies_not_equal() throws AnnotationParseException {
            java.lang.annotation.Annotation a1 = parser.parse("@NotNull");
            java.lang.annotation.Annotation a2 = parser.parse("@NotNull");
            assertThat(a1.equals(a2)).isFalse();
        }

        @org.junit.jupiter.api.Test
        void toString_contains_annotation_name() throws AnnotationParseException {
            java.lang.annotation.Annotation a = parser.parse("@NotNull");
            assertThat(a.toString()).contains("NotNull");
        }

        @org.junit.jupiter.api.Test
        void getDefaultValue_decimalMin_inclusive_defaults_to_true() throws AnnotationParseException {
            // inclusive is not in attrs map — falls through to method.getDefaultValue()
            DecimalMin dm = (DecimalMin) parser.parse("@DecimalMin(10.5)");
            assertThat(dm.inclusive()).isTrue();
        }

        @org.junit.jupiter.api.Test
        void getDefaultValue_pattern_flags_defaults_to_empty_array() throws AnnotationParseException {
            // flags is not in attrs map — falls through to method.getDefaultValue()
            Pattern p = (Pattern) parser.parse("@Pattern(regexp=\"^[A-Z]+$\")");
            assertThat(p.flags()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // No-argument annotations
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("NoArgumentAnnotation")
    class NoArgumentAnnotation {

        @Nested
        @DisplayName("ValidScenario")
        class ValidScenario {

            @ParameterizedTest(name = "[{index}] {0} → {1}")
            @MethodSource("valid")
            void testValidScenario(String input, Class<? extends Annotation> expectedType)
                    throws AnnotationParseException {
                Annotation result = parser.parse(input);
                assertThat(result).isNotNull();
                assertThat(result).isInstanceOf(expectedType);
                assertThat(result.annotationType()).isEqualTo(expectedType);
            }

            static Stream<Arguments> valid() {
                return Stream.of(
                        // presence — clean
                        Arguments.of("@NotNull", NotNull.class),
                        Arguments.of("@NotBlank", NotBlank.class),
                        Arguments.of("@NotEmpty", NotEmpty.class),
                        Arguments.of("@Null", Null.class),
                        // presence — spaces
                        Arguments.of("@NotNull ", NotNull.class),
                        Arguments.of(" @NotBlank", NotBlank.class),
                        Arguments.of("  @Null  ", Null.class),

                        // format — clean
                        Arguments.of("@Email", Email.class),
                        Arguments.of("@URL", URL.class),
                        Arguments.of("@CreditCardNumber", org.hibernate.validator.constraints.CreditCardNumber.class),
                        Arguments.of("@ISBN", org.hibernate.validator.constraints.ISBN.class),
                        Arguments.of("@EAN", org.hibernate.validator.constraints.EAN.class),
                        // format — spaces
                        Arguments.of("@Email ", Email.class),
                        Arguments.of(" @URL ", URL.class),

                        // sign — clean
                        Arguments.of("@Positive", Positive.class),
                        Arguments.of("@PositiveOrZero", PositiveOrZero.class),
                        Arguments.of("@Negative", Negative.class),
                        Arguments.of("@NegativeOrZero", NegativeOrZero.class),
                        // sign — spaces
                        Arguments.of("@Positive ", Positive.class),
                        Arguments.of(" @Negative ", Negative.class),

                        // temporal — clean
                        Arguments.of("@Past", Past.class),
                        Arguments.of("@PastOrPresent", PastOrPresent.class),
                        Arguments.of("@Future", Future.class),
                        Arguments.of("@FutureOrPresent", FutureOrPresent.class),
                        // temporal — spaces
                        Arguments.of("@Past ", Past.class),
                        Arguments.of(" @Future ", Future.class),

                        // boolean — clean
                        Arguments.of("@AssertTrue", AssertTrue.class),
                        Arguments.of("@AssertFalse", AssertFalse.class),
                        // boolean — spaces
                        Arguments.of("@AssertTrue ", AssertTrue.class),
                        Arguments.of(" @AssertFalse", AssertFalse.class)
                );
            }
        }

        @Nested
        @DisplayName("InvalidScenario")
        class InvalidScenario {

            @ParameterizedTest(name = "[{index}] \"{0}\"")
            @MethodSource("invalid")
            void testInvalidScenario(String input) {
                assertThatThrownBy(() -> parser.parse(input))
                        .isInstanceOf(AnnotationParseException.class);
            }

            static Stream<Arguments> invalid() {
                return Stream.of(
                        // unclosed paren
                        Arguments.of("@NotNull("),
                        Arguments.of("@Email("),
                        Arguments.of("@Past("),
                        Arguments.of("@AssertTrue("),
                        Arguments.of("@Positive("),
                        // extra closing paren
                        Arguments.of("@NotNull)"),
                        Arguments.of("@Future))"),
                        // misplaced brackets
                        Arguments.of("@)(NotNull"),
                        Arguments.of("@Past)("),
                        // unrecognised
                        Arguments.of("@UnknownNoArg"),
                        Arguments.of("@MyPresence"),
                        Arguments.of("@Valid(something=x)"),
                        Arguments.of("@NotNull(value=x)")
                );
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Single-argument annotations
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SingleArgumentAnnotation")
    class SingleArgumentAnnotation {

        @Nested
        @DisplayName("ValidScenario")
        class ValidScenario {

            @ParameterizedTest(name = "[{index}] {0} → {1} value={2}")
            @MethodSource("valid")
            void testValidScenario(String input,
                                   Class<? extends Annotation> expectedType,
                                   Object expectedValue)
                    throws AnnotationParseException {
                Annotation result = parser.parse(input);
                assertThat(result).isNotNull();
                assertThat(result).isInstanceOf(expectedType);
                assertThat(result.annotationType()).isEqualTo(expectedType);

                // verify the single attribute value via annotationType method
                try {
                    java.lang.reflect.Method valueMethod;
                    try {
                        valueMethod = expectedType.getDeclaredMethod("value");
                    } catch (NoSuchMethodException e) {
                        // Pattern uses regexp()
                        valueMethod = expectedType.getDeclaredMethod("regexp");
                    }
                    assertThat(valueMethod.invoke(result)).isEqualTo(expectedValue);
                } catch (Exception e) {
                    throw new RuntimeException("Could not verify value on " + expectedType.getSimpleName(), e);
                }
            }

            static Stream<Arguments> valid() {
                return Stream.of(
                        // @Min — positional
                        Arguments.of("@Min(18)", Min.class, 18L),
                        Arguments.of("@Min(0)", Min.class, 0L),
                        Arguments.of("@Min(-5)", Min.class, -5L),
                        Arguments.of("@Min(100)", Min.class, 100L),
                        // @Min — named, spaces
                        Arguments.of("@Min(value=18)", Min.class, 18L),
                        Arguments.of("@Min(value = 18)", Min.class, 18L),
                        Arguments.of("@Min( value = 0 )", Min.class, 0L),

                        // @Max — positional
                        Arguments.of("@Max(100)", Max.class, 100L),
                        Arguments.of("@Max(0)", Max.class, 0L),
                        // @Max — named, spaces
                        Arguments.of("@Max(value=100)", Max.class, 100L),
                        Arguments.of("@Max(value = 0)", Max.class, 0L),

                        // @DecimalMin — positional
                        Arguments.of("@DecimalMin(10.5)", DecimalMin.class, "10.5"),
                        Arguments.of("@DecimalMin(0.0)", DecimalMin.class, "0.0"),
                        Arguments.of("@DecimalMin(\"0.0\")", DecimalMin.class, "0.0"),
                        Arguments.of("@DecimalMin('0.0')", DecimalMin.class, "0.0"),
                        // @DecimalMin — named, spaces
                        Arguments.of("@DecimalMin(value=10.5)", DecimalMin.class, "10.5"),
                        Arguments.of("@DecimalMin(value = 10.5)", DecimalMin.class, "10.5"),

                        // @DecimalMax — positional
                        Arguments.of("@DecimalMax(999.99)", DecimalMax.class, "999.99"),
                        // @DecimalMax — named, spaces
                        Arguments.of("@DecimalMax(value=999.99)", DecimalMax.class, "999.99"),
                        Arguments.of("@DecimalMax(value = 999.99)", DecimalMax.class, "999.99"),

                        // @Pattern — named
                        Arguments.of("@Pattern(regexp=\"^[A-Z]+$\")", Pattern.class, "^[A-Z]+$"),
                        Arguments.of("@Pattern(regexp = \"^[A-Z]+$\")", Pattern.class, "^[A-Z]+$"),
                        Arguments.of("@Pattern(regexp=\"^[A-Z]{2}\\d+$\")", Pattern.class, "^[A-Z]{2}\\d+$"),
                        Arguments.of("@DecimalMin('10.5')", DecimalMin.class, "10.5"),
                        Arguments.of("@DecimalMax('999.99')", DecimalMax.class, "999.99")
                );
            }
        }

        @Nested
        @DisplayName("InvalidScenario")
        class InvalidScenario {

            @ParameterizedTest(name = "[{index}] \"{0}\"")
            @MethodSource("invalid")
            void testInvalidScenario(String input) {
                assertThatThrownBy(() -> parser.parse(input))
                        .isInstanceOf(AnnotationParseException.class);
            }

            static Stream<Arguments> invalid() {
                return Stream.of(
                        // @Min — missing value
                        Arguments.of("@Min()"),
                        Arguments.of("@Min(value=)"),
                        Arguments.of("@Min(value = )"),
                        // @Min — non-numeric
                        Arguments.of("@Min(abc)"),
                        Arguments.of("@Min(value=abc)"),
                        Arguments.of("@Min(1.5)"),
                        // @Min — malformed brackets
                        Arguments.of("@Min(10"),
                        Arguments.of("@Min(10))"),

                        // @Max — missing / invalid
                        Arguments.of("@Max()"),
                        Arguments.of("@Max(xyz)"),
                        Arguments.of("@Max(value=)"),

                        // @DecimalMin — missing
                        Arguments.of("@DecimalMin()"),
                        Arguments.of("@DecimalMin(value=)"),

                        // @DecimalMax — missing
                        Arguments.of("@DecimalMax()"),

                        // @Pattern — empty regexp
                        Arguments.of("@Pattern(regexp=)"),
                        Arguments.of("@Pattern(regexp=\"\")"),
                        Arguments.of("@Pattern(regexp='')"),
                        Arguments.of("@Pattern(regexp=' ')"),
                        Arguments.of("@Pattern(regexp=\"  \")"),
                        Arguments.of("@Pattern("),
                        Arguments.of("@Pattern(regexp=\""),
                        Arguments.of("@Min(wrong=18)"),
                        Arguments.of("@Max(wrong=100)")
                );
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Multiple-argument annotations
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MultipleArgumentAnnotation")
    class MultipleArgumentAnnotation {

        @Nested
        @DisplayName("ValidScenario")
        class ValidScenario {

            @ParameterizedTest(name = "[{index}] {0} → {1} v1={2} v2={3}")
            @MethodSource("valid")
            void testValidScenario(String input,
                                   Class<? extends Annotation> expectedType,
                                   String key1, Object expectedValue1,
                                   String key2, Object expectedValue2)
                    throws AnnotationParseException {
                Annotation result = parser.parse(input);
                assertThat(result).isNotNull();
                assertThat(result).isInstanceOf(expectedType);
                assertThat(result.annotationType()).isEqualTo(expectedType);

                try {
                    Object v1 = expectedType.getDeclaredMethod(key1).invoke(result);
                    Object v2 = expectedType.getDeclaredMethod(key2).invoke(result);
                    assertThat(v1).isEqualTo(expectedValue1);
                    assertThat(v2).isEqualTo(expectedValue2);
                } catch (Exception e) {
                    throw new RuntimeException("Could not verify attributes on " + expectedType.getSimpleName(), e);
                }
            }

            static Stream<Arguments> valid() {
                return Stream.of(
                        // @Size — clean
                        Arguments.of("@Size(min=1, max=10)", Size.class, "min", 1, "max", 10),
                        Arguments.of("@Size(min=0, max=2147483647)", Size.class, "min", 0, "max", Integer.MAX_VALUE),
                        Arguments.of("@Size(min=5, max=100)", Size.class, "min", 5, "max", 100),
                        // @Size — spaces
                        Arguments.of("@Size(min = 1, max = 10)", Size.class, "min", 1, "max", 10),
                        Arguments.of("@Size( min = 0 , max = 50 )", Size.class, "min", 0, "max", 50),
                        // @Size — reversed order
                        Arguments.of("@Size(max=10, min=1)", Size.class, "min", 1, "max", 10),

                        // @Length — clean
                        Arguments.of("@Length(min=3, max=20)", Length.class, "min", 3, "max", 20),
                        Arguments.of("@Length(min=0, max=255)", Length.class, "min", 0, "max", 255),
                        // @Length — spaces
                        Arguments.of("@Length(min = 3, max = 20)", Length.class, "min", 3, "max", 20),
                        Arguments.of("@Length( min=1 , max=50 )", Length.class, "min", 1, "max", 50),
                        Arguments.of("@Length( max=50 , min=1 )", Length.class, "min", 1, "max", 50),

                        // @Digits — clean
                        Arguments.of("@Digits(integer=8, fraction=2)", Digits.class, "integer", 8, "fraction", 2),
                        Arguments.of("@Digits(integer=5, fraction=0)", Digits.class, "integer", 5, "fraction", 0),
                        Arguments.of("@Digits(integer=1, fraction=10)", Digits.class, "integer", 1, "fraction", 10),
                        // @Digits — spaces
                        Arguments.of("@Digits(integer = 8, fraction = 2)", Digits.class, "integer", 8, "fraction", 2),
                        Arguments.of("@Digits( integer=5 , fraction=0 )", Digits.class, "integer", 5, "fraction", 0),
                        // @Digits — reversed order
                        Arguments.of("@Digits(fraction=2, integer=8)", Digits.class, "integer", 8, "fraction", 2)
                );
            }
        }

        @Nested
        @DisplayName("InvalidScenario")
        class InvalidScenario {

            @ParameterizedTest(name = "[{index}] \"{0}\"")
            @MethodSource("invalid")
            void testInvalidScenario(String input) {
                assertThatThrownBy(() -> parser.parse(input))
                        .isInstanceOf(AnnotationParseException.class);
            }

            static Stream<Arguments> invalid() {
                return Stream.of(
                        // @Size — non-numeric
                        Arguments.of("@Size(min=abc, max=10)"),
                        Arguments.of("@Size(min=1, max=xyz)"),
                        // @Size — empty values
                        Arguments.of("@Size(min=, max=10)"),
                        Arguments.of("@Size(min=1, max=)"),
                        // @Size — malformed brackets
                        Arguments.of("@Size(min=1, max=10"),
                        Arguments.of("@Size(min=1, max=10))"),

                        // @Length — non-numeric
                        Arguments.of("@Length(min=abc, max=20)"),
                        Arguments.of("@Length(min=3, max=xyz)"),
                        // @Length — empty values
                        Arguments.of("@Length(min=, max=20)"),
                        Arguments.of("@Length(min=3, max=)"),
                        // @Length — malformed brackets
                        Arguments.of("@Length(min=3, max=20"),

                        // @Digits — non-numeric
                        Arguments.of("@Digits(integer=abc, fraction=2)"),
                        Arguments.of("@Digits(integer=8, fraction=xyz)"),
                        // @Digits — empty
                        Arguments.of("@Digits()"),
                        Arguments.of("@Digits(integer=, fraction=2)"),
                        Arguments.of("@Digits(integer=8, fraction=)"),
                        // @Digits — malformed brackets
                        Arguments.of("@Digits(integer=8, fraction=2"),
                        Arguments.of("@Digits(integer=8, fraction=2))"),
                        Arguments.of("@Size(min1, max=10)"),
                        Arguments.of("@Length(min1, max=20)"),
                        Arguments.of("@Digits(integer8, fraction=2)")
                );
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // parseAll
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ParseAll")
    class ParseAll {

        @Nested
        @DisplayName("ValidScenario")
        class ValidScenario {

            @ParameterizedTest(name = "[{index}] {0}")
            @MethodSource("valid")
            void testValidScenario(List<String> inputs, int expectedSize)
                    throws AnnotationParseException {
                List<Annotation> result = parser.parseAll(inputs);
                assertThat(result).hasSize(expectedSize);
                result.forEach(a -> assertThat(a.annotationType()).isNotNull());
            }

            static Stream<Arguments> valid() {
                return Stream.of(
                        // single items
                        Arguments.of(List.of("@NotNull"), 1),
                        Arguments.of(List.of("@Min(18)"), 1),
                        Arguments.of(List.of("@Size(min=1, max=10)"), 1),
                        Arguments.of(List.of("@Pattern(regexp=\"^[A-Z]+$\")"), 1),

                        // mixed types
                        Arguments.of(List.of("@NotNull", "@Min(18)", "@Email"), 3),
                        Arguments.of(List.of("@Past", "@NotBlank", "@Size(min=1, max=50)"), 3),
                        Arguments.of(List.of("@AssertTrue", "@Positive"), 2),
                        Arguments.of(List.of("@Digits(integer=8, fraction=2)", "@DecimalMin(0.5)"), 2),

                        // all no-arg
                        Arguments.of(List.of("@NotNull", "@NotBlank", "@NotEmpty"), 3),

                        // all single-arg
                        Arguments.of(List.of("@Min(1)", "@Max(100)"), 2),

                        // all multi-arg
                        Arguments.of(List.of("@Size(min=1, max=10)", "@Length(min=3, max=20)", "@Digits(integer=5, fraction=2)"), 3),

                        // with spaces around annotations
                        Arguments.of(List.of(" @NotNull ", " @Min(18) ", " @Email "), 3),

                        // empty list
                        Arguments.of(List.of(), 0)
                );
            }
        }

        @Nested
        @DisplayName("InvalidScenario")
        class InvalidScenario {

            @ParameterizedTest(name = "[{index}] {0}")
            @MethodSource("invalid")
            void testInvalidScenario(List<String> inputs) {
                assertThatThrownBy(() -> parser.parseAll(inputs))
                        .isInstanceOf(AnnotationParseException.class);
            }

            static Stream<Arguments> invalid() {
                return Stream.of(
                        Arguments.of(List.of("@Min()", "@NotNull", "@Email")),           // bad first
                        Arguments.of(List.of("@NotNull", "@Min()", "@Email")),           // bad middle
                        Arguments.of(List.of("@NotNull", "@Email", "@Min()")),           // bad last
                        Arguments.of(List.of("@Unknown", "@NotNull", "@Email")),         // unrecognised first
                        Arguments.of(List.of("@NotNull", "@Unknown", "@Email")),         // unrecognised middle
                        Arguments.of(List.of("@Min()", "@Max()", "@Digits()"))           // all bad
                );
            }
        }
    }
}
