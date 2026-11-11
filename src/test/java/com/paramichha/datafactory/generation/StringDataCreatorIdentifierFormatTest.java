package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Strings that must have an ISBN or EAN format. */
@DisplayName("StringDataCreator — identifier formats")
class StringDataCreatorIdentifierFormatTest {

    @Test
    @DisplayName("an @ISBN string is generated")
    void isbnStringIsGenerated() {
        var fc = AnnotationResolver.analyzeFromStrings("isbn", "String", List.of("@ISBN"));
        var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
        assertThat(v).isNotBlank();
    }

    @Test
    @DisplayName("an @EAN string is generated")
    void eanStringIsGenerated() {
        var fc = AnnotationResolver.analyzeFromStrings("ean", "String", List.of("@EAN"));
        var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
        assertThat(v).isNotBlank();
    }
}
