package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.planner.BoundaryTarget;
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
        assertThat(CharacterTypeShaper.INSTANCE.supports(Character.class)).isTrue();
    }

    @Test
    void supports_char() {
        assertThat(CharacterTypeShaper.INSTANCE.supports(char.class)).isTrue();
    }

    @Test
    void supports_String_false() {
        assertThat(CharacterTypeShaper.INSTANCE.supports(String.class)).isFalse();
    }

    @Test
    void shape_returns_char() {
        var fc = AnnotationAnalyzer.analyzeFromStrings("c", "Character", List.of());
        var result = CharacterTypeShaper.INSTANCE.shape(fc, BoundaryTarget.semantic());
        assertThat(result).isInstanceOf(Character.class);
    }

    @Test
    void shapeAll_via_value_generator() {
        var fc = AnnotationAnalyzer.analyzeFromStrings("c", "char", List.of());
        var targets = List.of(BoundaryTarget.semantic());
        var result = ValueGenerator.shapeAll(fc, targets);
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(Character.class);
    }
}
