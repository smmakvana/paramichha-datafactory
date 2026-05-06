package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.generation.DataMachines;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        var result = CharacterDataCreator.INSTANCE.shape(fc, BoundaryTarget.semantic(, DataMachines.random()));
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
}
