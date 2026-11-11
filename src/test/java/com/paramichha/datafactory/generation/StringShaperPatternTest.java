package com.paramichha.datafactory.generation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Branch coverage for StringShaper.generateFromPattern() —
 * every quantifier, escape sequence, and character class variant.
 */
@DisplayName("StringShaper — generateFromPattern() branches")
class StringShaperPatternTest {

    @Nested
    @DisplayName("Construction")
    class Construction {
        @Test
        @DisplayName("null returns A")
        void nullReturnsA() {
            assertThat(StringDataCreator.generateFromPattern(null)).isEqualTo("A");
        }

        @Test
        @DisplayName("empty string returns A")
        void emptyReturnsA() {
            assertThat(StringDataCreator.generateFromPattern("")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Validation — anchors stripped")
    class Anchors {
        @Test
        @DisplayName("^ prefix stripped")
        void caretStripped() {
            assertThat(StringDataCreator.generateFromPattern("^A")).isEqualTo("A");
        }

        @Test
        @DisplayName("$ suffix stripped")
        void dollarStripped() {
            assertThat(StringDataCreator.generateFromPattern("A$")).isEqualTo("A");
        }

        @Test
        @DisplayName("both anchors stripped")
        void bothStripped() {
            assertThat(StringDataCreator.generateFromPattern("^A$")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Validation — alternation")
    class Alternation {
        @Test
        @DisplayName("FOO|BAR returns FOO")
        void simplePipe() {
            assertThat(StringDataCreator.generateFromPattern("FOO|BAR")).isEqualTo("FOO");
        }

        @Test
        @DisplayName("(A|B) strips parens and returns A")
        void parenPipe() {
            assertThat(StringDataCreator.generateFromPattern("(A|B)")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Validation — quantifiers")
    class Quantifiers {
        @Test
        @DisplayName("* produces 2 chars")
        void star() {
            assertThat(StringDataCreator.generateFromPattern("A*")).hasSize(2);
        }

        @Test
        @DisplayName("+ produces 2 chars")
        void plus() {
            assertThat(StringDataCreator.generateFromPattern("A+")).hasSize(2);
        }

        @Test
        @DisplayName("? produces 1 char")
        void question() {
            assertThat(StringDataCreator.generateFromPattern("A?")).hasSize(1);
        }

        @Test
        @DisplayName("{n} produces exactly n chars")
        void exact() {
            assertThat(StringDataCreator.generateFromPattern("A{5}")).hasSize(5);
        }

        @Test
        @DisplayName("{n,m} uses n as minimum")
        void range() {
            assertThat(StringDataCreator.generateFromPattern("A{2,5}")).hasSize(2);
        }

        @Test
        @DisplayName("{bad} falls back to 1")
        void badQuantifier() {
            assertThat(StringDataCreator.generateFromPattern("A{bad}")).hasSize(1);
        }

        @Test
        @DisplayName("{ unclosed — A is literal, { is literal")
        void unclosedBrace() {
            assertThat(StringDataCreator.generateFromPattern("A{")).isEqualTo("A{");
        }
    }

    @Nested
    @DisplayName("Validation — escape sequences")
    class Escapes {
        @Test
        @DisplayName("\\d produces digit")
        void digit() {
            assertThat(StringDataCreator.generateFromPattern("\\d")).isEqualTo("5");
        }

        @Test
        @DisplayName("\\D produces non-digit")
        void nonDigit() {
            assertThat(StringDataCreator.generateFromPattern("\\D")).isEqualTo("A");
        }

        @Test
        @DisplayName("\\w produces word char")
        void word() {
            assertThat(StringDataCreator.generateFromPattern("\\w")).isEqualTo("a");
        }

        @Test
        @DisplayName("\\W produces non-word char")
        void nonWord() {
            assertThat(StringDataCreator.generateFromPattern("\\W")).isEqualTo("!");
        }

        @Test
        @DisplayName("\\s produces space")
        void space() {
            assertThat(StringDataCreator.generateFromPattern("\\s")).isEqualTo(" ");
        }

        @Test
        @DisplayName("\\- produces literal -")
        void literalEscape() {
            assertThat(StringDataCreator.generateFromPattern("\\-")).isEqualTo("-");
        }

        @Test
        @DisplayName("\\d{3} produces 3 digits")
        void digitWithQuantifier() {
            assertThat(StringDataCreator.generateFromPattern("\\d{3}")).isEqualTo("555");
        }
    }

    @Nested
    @DisplayName("Validation — character classes")
    class CharacterClasses {
        @Test
        @DisplayName("[A-Z] returns A")
        void upperRange() {
            assertThat(StringDataCreator.generateFromPattern("[A-Z]")).isEqualTo("A");
        }

        @Test
        @DisplayName("[a-z] returns a")
        void lowerRange() {
            assertThat(StringDataCreator.generateFromPattern("[a-z]")).isEqualTo("a");
        }

        @Test
        @DisplayName("[0-9] returns 5")
        void digitRange() {
            assertThat(StringDataCreator.generateFromPattern("[0-9]")).isEqualTo("5");
        }

        @Test
        @DisplayName("[AB] returns A (first explicit char)")
        void explicitChars() {
            assertThat(StringDataCreator.generateFromPattern("[AB]")).isEqualTo("A");
        }

        @Test
        @DisplayName("[^0-9] returns X (negated)")
        void negated() {
            assertThat(StringDataCreator.generateFromPattern("[^0-9]")).isEqualTo("X");
        }

        @Test
        @DisplayName("[A-Z]{3} returns AAA")
        void withQuantifier() {
            assertThat(StringDataCreator.generateFromPattern("[A-Z]{3}")).isEqualTo("AAA");
        }

        @Test
        @DisplayName("[ unclosed appends A and continues")
        void unclosedClass() {
            assertThat(StringDataCreator.generateFromPattern("[unclosed")).isEqualTo("Aunclosed");
        }
    }

    @Nested
    @DisplayName("Validation — special chars")
    class SpecialChars {
        @Test
        @DisplayName(". wildcard produces a")
        void dot() {
            assertThat(StringDataCreator.generateFromPattern(".")).isEqualTo("a");
        }

        @Test
        @DisplayName(".{3} produces aaa")
        void dotWithQuantifier() {
            assertThat(StringDataCreator.generateFromPattern(".{3}")).isEqualTo("aaa");
        }

        @Test
        @DisplayName("( and ) skipped")
        void parensSkipped() {
            assertThat(StringDataCreator.generateFromPattern("(A)")).isEqualTo("A");
        }

        @Test
        @DisplayName("empty result after processing returns A")
        void emptyResultReturnsA() {
            assertThat(StringDataCreator.generateFromPattern("()")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Behaviour — exception fallback")
    class ExceptionFallback {
        @Test
        @DisplayName("malformed pattern returns A not exception")
        void malformedReturnsFallback() {
            assertThat(StringDataCreator.generateFromPattern("[[[")).isEqualTo("AAA");
        }
    }
}
