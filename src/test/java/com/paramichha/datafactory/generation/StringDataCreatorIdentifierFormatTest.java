package com.paramichha.datafactory.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Strings that must have an ISBN or EAN format. */
@DisplayName("StringDataCreator — identifier formats")
class StringDataCreatorIdentifierFormatTest {

    @Test
    @DisplayName("an @ISBN string is generated")
    void isbnStringIsGenerated() {
        var fc = AnnotationResolver.analyzeFromStrings("isbn", "String", List.of("@ISBN"));
        var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
        assertValidEan13(v);
    }

    @Test
    @DisplayName("an @EAN string is generated")
    void eanStringIsGenerated() {
        var fc = AnnotationResolver.analyzeFromStrings("ean", "String", List.of("@EAN"));
        var v = StringDataCreator.getString(fc, BoundaryTarget.semantic(), DataMachines.random());
        assertValidEan13(v);
    }

    /** ISBN-13 and EAN-13 share one check: digits weighted 1, 3, 1, 3 ... sum to a multiple of 10. */
    private static void assertValidEan13(String v) {
        assertThat(v).matches("\\d{13}");
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (v.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        }
        assertThat(sum % 10).as("check digit of %s", v).isZero();
    }
}
