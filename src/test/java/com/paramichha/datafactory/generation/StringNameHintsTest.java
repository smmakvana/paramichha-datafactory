package com.paramichha.datafactory.generation;

import static com.paramichha.datafactory.generation.ShaperTestSupport.fieldOf;
import static com.paramichha.datafactory.generation.ShaperTestSupport.semantic;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** A String field with no constraints gets a value that suits its name. */
@DisplayName("StringDataCreator — values chosen by field name")
class StringNameHintsTest {

    private static final long SEED = 7L;

    private static Object valueFor(String fieldName) {
        return StringDataCreator.INSTANCE.shape(
                fieldOf(fieldName, String.class), semantic(), DataMachines.seeded(SEED));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "recipient, ip",
        "role, jobtitle",
        "position, jobtitle",
        "salutation, prefix",
        "suffix, suffix",
        "nationality, nationality",
        "language, language",
        "guidText, uuid",
    })
    @DisplayName("the field name picks the kind of text")
    void fieldNamePicksTheKindOfText(String fieldName, String kind) {
        assertThat(valueFor(fieldName)).isEqualTo(DataMachines.seeded(SEED).text(kind));
    }
}
