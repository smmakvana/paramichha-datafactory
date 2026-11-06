package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.constraint.FieldConstraints;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/** Name hints and the char, array and map creators. */
class HintsAndCreatorsTest {

    private static final Faker FAKER = new Faker(new Random(5));

    @ParameterizedTest(name = "{0}")
    @EnumSource(DataHint.class)
    @DisplayName("every hint produces a value")
    void everyHintProducesAValue(DataHint hint) {
        assertThat(hint.produce(FAKER)).isNotBlank();
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "nickname, USERNAME", "salutation, PREFIX", "suffix, SUFFIX", "totalAmount, PRICE",
            "transactionId, ORDER_ID", "bankCode, ACCOUNT_CODE", "kind, TAG", "otp, CODE",
            "voucherCode, REFERENCE", "bio, PARAGRAPH", "lang, LANGUAGE", "nationality, NATIONALITY",
            "somethingElse, WORD", "Email, EMAIL", "ipAddress, IP_ADDRESS"})
    @DisplayName("names map to hints, case-insensitively, with WORD as the fallback")
    void namesMapToHints(String name, DataHint expected) {
        assertThat(DataHint.fromVariableName(name)).isEqualTo(expected);
    }

    @Test
    void blankOrNullNamesFallBackToWord() {
        assertThat(DataHint.fromVariableName(null)).isEqualTo(DataHint.WORD);
        assertThat(DataHint.fromVariableName(" ")).isEqualTo(DataHint.WORD);
    }

    @Test
    @DisplayName("a char with a [abc] pattern picks one of those characters")
    void characterFromPatternClass() {
        assertThat(DataFactory.character().with("@Pattern(regexp=\"[xyz]\")").validList())
                .isNotEmpty().allSatisfy(c -> assertThat(c).isIn('x', 'y', 'z'));
    }

    @Test
    @DisplayName("an array under @Size stays within its bounds")
    void arrayRespectsSize() {
        assertThat(DataFactory.field(String[].class).with("@Size(min=2, max=3)").validList())
                .isNotEmpty().allSatisfy(a -> assertThat(a.length).isBetween(2, 3));
    }

    @Test
    @DisplayName("a raw map field gets String keys and values")
    void mapCreator() {
        FieldConstraints field = FieldConstraints.builder("headers", Map.class).build();
        Object value = CollectionDataCreator.INSTANCE.shape(field, BoundaryTarget.semantic(),
                DataMachines.seeded(3L));
        assertThat(value).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) value).isNotEmpty().allSatisfy((k, v) -> {
            assertThat(k).isInstanceOf(String.class);
            assertThat(v).isInstanceOf(String.class);
        });
        assertThat(CollectionDataCreator.INSTANCE.supports(List.class)).isTrue();
        assertThat(CollectionDataCreator.INSTANCE.supports(String.class)).isFalse();
    }

    @Test
    void aPatternTheGeneratorCannotReadFallsBackToA() {
        // "|" splits into no alternatives at all
        assertThat(StringDataCreator.generateFromPattern("|")).isEqualTo("A");
    }
}
