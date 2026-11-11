package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A URL that must fit a @Size bound: trimmed when too long, padded when too short. */
@DisplayName("StringDataCreator — fitting a URL to a size")
class StringDataCreatorUrlFitTest {

    @Test
    @DisplayName("a URL longer than the maximum is trimmed to it")
    void longUrlIsTrimmedToTheMaximum() {
        var fc = AnnotationResolver.analyzeFromStrings("site", "String",
                List.of("@URL", "@Size(min=5, max=20)"));
        var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
        assertThat(v.length()).isLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("a URL shorter than the minimum is padded to it")
    void shortUrlIsPaddedToTheMinimum() {
        var fc = AnnotationResolver.analyzeFromStrings("site", "String",
                List.of("@URL", "@Size(min=50, max=200)"));
        var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
        assertThat(v.length()).isGreaterThanOrEqualTo(50);
    }
}
