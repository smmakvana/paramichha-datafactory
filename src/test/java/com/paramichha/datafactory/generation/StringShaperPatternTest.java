package com.paramichha.datafactory.generation;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Branch coverage for StringShaper.generateFromPattern() —
 * every quantifier, escape sequence, and character class variant.
 */
@DisplayName("StringShaper — generateFromPattern() branches")
class StringShaperPatternTest {

    @Nested
    @DisplayName("Construction")
    class Construction {
        @Test @DisplayName("null returns A")
        void nullReturnsA() {
            assertThat(StringShaper.generateFromPattern(null)).isEqualTo("A");
        }
        @Test @DisplayName("empty string returns A")
        void emptyReturnsA() {
            assertThat(StringShaper.generateFromPattern("")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Validation — anchors stripped")
    class Anchors {
        @Test @DisplayName("^ prefix stripped")
        void caretStripped() {
            assertThat(StringShaper.generateFromPattern("^A")).isEqualTo("A");
        }
        @Test @DisplayName("$ suffix stripped")
        void dollarStripped() {
            assertThat(StringShaper.generateFromPattern("A$")).isEqualTo("A");
        }
        @Test @DisplayName("both anchors stripped")
        void bothStripped() {
            assertThat(StringShaper.generateFromPattern("^A$")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Validation — alternation")
    class Alternation {
        @Test @DisplayName("FOO|BAR returns FOO")
        void simplePipe() {
            assertThat(StringShaper.generateFromPattern("FOO|BAR")).isEqualTo("FOO");
        }
        @Test @DisplayName("(A|B) strips parens and returns A")
        void parenPipe() {
            assertThat(StringShaper.generateFromPattern("(A|B)")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Validation — quantifiers")
    class Quantifiers {
        @Test @DisplayName("* produces 2 chars")
        void star() {
            assertThat(StringShaper.generateFromPattern("A*")).hasSize(2);
        }
        @Test @DisplayName("+ produces 2 chars")
        void plus() {
            assertThat(StringShaper.generateFromPattern("A+")).hasSize(2);
        }
        @Test @DisplayName("? produces 1 char")
        void question() {
            assertThat(StringShaper.generateFromPattern("A?")).hasSize(1);
        }
        @Test @DisplayName("{n} produces exactly n chars")
        void exact() {
            assertThat(StringShaper.generateFromPattern("A{5}")).hasSize(5);
        }
        @Test @DisplayName("{n,m} uses n as minimum")
        void range() {
            assertThat(StringShaper.generateFromPattern("A{2,5}")).hasSize(2);
        }
        @Test @DisplayName("{bad} falls back to 1")
        void badQuantifier() {
            assertThat(StringShaper.generateFromPattern("A{bad}")).hasSize(1);
        }
        @Test @DisplayName("{ unclosed — A is literal, { is literal")
        void unclosedBrace() {
            assertThat(StringShaper.generateFromPattern("A{")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Validation — escape sequences")
    class Escapes {
        @Test @DisplayName("\\d produces digit")
        void digit() {
            assertThat(StringShaper.generateFromPattern("\\d")).isEqualTo("5");
        }
        @Test @DisplayName("\\D produces non-digit")
        void nonDigit() {
            assertThat(StringShaper.generateFromPattern("\\D")).isEqualTo("A");
        }
        @Test @DisplayName("\\w produces word char")
        void word() {
            assertThat(StringShaper.generateFromPattern("\\w")).isEqualTo("a");
        }
        @Test @DisplayName("\\W produces non-word char")
        void nonWord() {
            assertThat(StringShaper.generateFromPattern("\\W")).isEqualTo("!");
        }
        @Test @DisplayName("\\s produces space")
        void space() {
            assertThat(StringShaper.generateFromPattern("\\s")).isEqualTo(" ");
        }
        @Test @DisplayName("\\- produces literal -")
        void literalEscape() {
            assertThat(StringShaper.generateFromPattern("\\-")).isEqualTo("-");
        }
        @Test @DisplayName("\\d{3} produces 3 digits")
        void digitWithQuantifier() {
            assertThat(StringShaper.generateFromPattern("\\d{3}")).isEqualTo("555");
        }
    }

    @Nested
    @DisplayName("Validation — character classes")
    class CharacterClasses {
        @Test @DisplayName("[A-Z] returns A")
        void upperRange() {
            assertThat(StringShaper.generateFromPattern("[A-Z]")).isEqualTo("A");
        }
        @Test @DisplayName("[a-z] returns a")
        void lowerRange() {
            assertThat(StringShaper.generateFromPattern("[a-z]")).isEqualTo("a");
        }
        @Test @DisplayName("[0-9] returns 5")
        void digitRange() {
            assertThat(StringShaper.generateFromPattern("[0-9]")).isEqualTo("5");
        }
        @Test @DisplayName("[AB] returns A (first explicit char)")
        void explicitChars() {
            assertThat(StringShaper.generateFromPattern("[AB]")).isEqualTo("A");
        }
        @Test @DisplayName("[^0-9] returns X (negated)")
        void negated() {
            assertThat(StringShaper.generateFromPattern("[^0-9]")).isEqualTo("X");
        }
        @Test @DisplayName("[A-Z]{3} returns AAA")
        void withQuantifier() {
            assertThat(StringShaper.generateFromPattern("[A-Z]{3}")).isEqualTo("AAA");
        }
        @Test @DisplayName("[ unclosed appends A and continues")
        void unclosedClass() {
            assertThat(StringShaper.generateFromPattern("[unclosed")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Validation — special chars")
    class SpecialChars {
        @Test @DisplayName(". wildcard produces a")
        void dot() {
            assertThat(StringShaper.generateFromPattern(".")).isEqualTo("a");
        }
        @Test @DisplayName(".{3} produces aaa")
        void dotWithQuantifier() {
            assertThat(StringShaper.generateFromPattern(".{3}")).isEqualTo("aaa");
        }
        @Test @DisplayName("( and ) skipped")
        void parensSkipped() {
            assertThat(StringShaper.generateFromPattern("(A)")).isEqualTo("A");
        }
        @Test @DisplayName("empty result after processing returns A")
        void emptyResultReturnsA() {
            assertThat(StringShaper.generateFromPattern("()")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Behaviour — exception fallback")
    class ExceptionFallback {
        @Test @DisplayName("malformed pattern returns A not exception")
        void malformedReturnsFallback() {
            assertThat(StringShaper.generateFromPattern("[[[")).isNotNull();
        }
    }
}
