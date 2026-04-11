package com.paramichha.testkit.clazz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClazzParserTest {

    private ClazzParser parser;

    @BeforeEach
    void setup() {
        parser = new DefaultClazzParser();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SmokeTest
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SmokeTest")
    class SmokeTest {

        @Nested
        @DisplayName("ValidScenario")
        class ValidScenario {

            @ParameterizedTest(name = "[{index}] \"{0}\" → {1}")
            @MethodSource("valid")
            void parsesSuccessfully(String input, Class<?> expectedType) throws ClazzParserException {
                Class<?> result = parser.parse(input);
                assertThat(result).isEqualTo(expectedType);
            }

            static Stream<Arguments> valid() {
                return Stream.of(
                        // primitives
                        Arguments.of("int", int.class),
                        Arguments.of("long", long.class),
                        Arguments.of("double", double.class),
                        Arguments.of("float", float.class),
                        Arguments.of("boolean", boolean.class),
                        Arguments.of("char", char.class),
                        Arguments.of("short", short.class),
                        Arguments.of("byte", byte.class),
                        // wrappers
                        Arguments.of("Integer", Integer.class),
                        Arguments.of("Long", Long.class),
                        Arguments.of("Double", Double.class),
                        Arguments.of("Float", Float.class),
                        Arguments.of("Boolean", Boolean.class),
                        Arguments.of("Character", Character.class),
                        Arguments.of("Short", Short.class),
                        Arguments.of("Byte", Byte.class),
                        // common
                        Arguments.of("String", String.class),
                        Arguments.of("BigDecimal", java.math.BigDecimal.class),
                        Arguments.of("BigInteger", java.math.BigInteger.class),
                        // temporal
                        Arguments.of("LocalDate", java.time.LocalDate.class),
                        Arguments.of("LocalDateTime", java.time.LocalDateTime.class),
                        Arguments.of("LocalTime", java.time.LocalTime.class),
                        Arguments.of("ZonedDateTime", java.time.ZonedDateTime.class),
                        Arguments.of("OffsetDateTime", java.time.OffsetDateTime.class),
                        Arguments.of("Instant", java.time.Instant.class),
                        Arguments.of("Year", java.time.Year.class),
                        Arguments.of("YearMonth", java.time.YearMonth.class),
                        // other
                        Arguments.of("UUID", java.util.UUID.class),
                        Arguments.of("Date", java.util.Date.class),
                        Arguments.of("List", java.util.List.class),
                        Arguments.of("Collection", java.util.Collection.class),
                        // leading/trailing spaces
                        Arguments.of(" int ", int.class),
                        Arguments.of(" String ", String.class),
                        Arguments.of(" Long ", Long.class)
                );
            }
        }

        @Nested
        @DisplayName("InvalidScenario")
        class InvalidScenario {

            @ParameterizedTest(name = "[{index}] \"{0}\"")
            @MethodSource("invalid")
            void throwsOnInvalid(String input) {
                assertThatThrownBy(() -> parser.parse(input))
                        .isInstanceOf(ClazzParserException.class);
            }

            static Stream<Arguments> invalid() {
                return Stream.of(
                        Arguments.of((Object) null),   // null
                        Arguments.of(""),              // blank
                        Arguments.of("   "),           // blank spaces
                        Arguments.of("Foo"),           // unrecognised
                        Arguments.of("integer"),       // wrong case
                        Arguments.of("INT"),           // wrong case
                        Arguments.of("string"),        // wrong case
                        Arguments.of("localdate"),     // wrong case
                        Arguments.of("java.lang.String") // fully qualified not supported
                );
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ParseAll
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ParseAll")
    class ParseAll {

        @Nested
        @DisplayName("ValidScenario")
        class ValidScenario {

            @ParameterizedTest(name = "[{index}] {0}")
            @MethodSource("valid")
            void parsesAll(List<String> inputs, List<Class<?>> expectedTypes) throws ClazzParserException {
                List<Class<?>> result = parser.parseAll(inputs);
                assertThat(result).isEqualTo(expectedTypes);
            }

            static Stream<Arguments> valid() {
                return Stream.of(
                        Arguments.of(List.of("int", "String", "Long"),
                                List.of(int.class, String.class, Long.class)),
                        Arguments.of(List.of("LocalDate", "Boolean", "BigDecimal"),
                                List.of(java.time.LocalDate.class, Boolean.class, java.math.BigDecimal.class)),
                        Arguments.of(List.of(), List.of())
                );
            }
        }

        @Nested
        @DisplayName("InvalidScenario")
        class InvalidScenario {

            @ParameterizedTest(name = "[{index}] {0}")
            @MethodSource("invalid")
            void failsFastOnInvalid(List<String> inputs) {
                assertThatThrownBy(() -> parser.parseAll(inputs))
                        .isInstanceOf(ClazzParserException.class);
            }

            static Stream<Arguments> invalid() {
                return Stream.of(
                        Arguments.of(List.of("Foo", "String", "int")),   // bad first
                        Arguments.of(List.of("String", "Foo", "int")),   // bad middle
                        Arguments.of(List.of("String", "int", "Foo"))    // bad last
                );
            }
        }
    }
}
