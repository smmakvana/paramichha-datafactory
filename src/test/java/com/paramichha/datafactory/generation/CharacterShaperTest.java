package com.paramichha.datafactory.generation;

import static com.paramichha.datafactory.generation.ShaperTestSupport.fieldOf;
import static com.paramichha.datafactory.generation.ShaperTestSupport.semantic;
import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Covers CharacterTypeShaper — supports() and shape().
 */
@DisplayName("CharacterTypeShaper")
class CharacterShaperTest {

    @Test
    void supports_Character() {
        assertThat(CharacterDataCreator.INSTANCE.supports(Character.class)).isTrue();
    }

    @Test
    void supports_char() {
        assertThat(CharacterDataCreator.INSTANCE.supports(char.class)).isTrue();
    }

    @Test
    void supports_String_false() {
        assertThat(CharacterDataCreator.INSTANCE.supports(String.class)).isFalse();
    }

    @Test
    void shape_returns_char() {
        var fc = AnnotationResolver.analyzeFromStrings("c", "Character", List.of());
        var result = CharacterDataCreator.INSTANCE.shape(fc, BoundaryTarget.semantic(), DataMachines.random());
        assertThat(result).isInstanceOf(Character.class);
    }

    @Test
    void shapeAll_via_value_generator() {
        var fc = AnnotationResolver.analyzeFromStrings("c", "char", List.of());
        var targets = List.of(BoundaryTarget.semantic());
        var result = DataGenerator.shapeAll(fc, targets, DataMachines.random());
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(Character.class);
    }

    @Nested
    @DisplayName("CharacterTypeShaper")
    class CharacterValues {
        @Test
        void supportsCharacter() {
            assertThat(CharacterDataCreator.INSTANCE.supports(Character.class)).isTrue();
        }

        @Test
        void supportsCharPrimitive() {
            assertThat(CharacterDataCreator.INSTANCE.supports(char.class)).isTrue();
        }

        @Test
        void supportsStringFalse() {
            assertThat(CharacterDataCreator.INSTANCE.supports(String.class)).isFalse();
        }

        @Test
        void shapeReturnsChar() {
            var f = fieldOf("c", Character.class);
            assertThat(CharacterDataCreator.INSTANCE.shape(f, semantic(), DataMachines.random()))
                    .isInstanceOf(Character.class);
        }

        @ParameterizedTest(name = "{0} is one of {1}")
        @CsvSource({"gender, MF", "examGrade, ABCDE", "activeFlag, YN"})
        @DisplayName("the field name picks the letters to choose from")
        void fieldNamePicksTheLetters(String fieldName, String letters) {
            for (long seed = 0; seed < 20; seed++) {
                Object value = CharacterDataCreator.INSTANCE.shape(
                        fieldOf(fieldName, Character.class), semantic(), DataMachines.seeded(seed));
                assertThat(letters).contains(String.valueOf(value));
            }
        }

        @Test
        @DisplayName("a field that must be null gets null")
        void nullTargetGivesNull() {
            var f = fieldOf("c", Character.class);
            assertThat(CharacterDataCreator.INSTANCE.shape(f, BoundaryTarget.nullTarget(), DataMachines.random()))
                    .isNull();
        }
    }
}
