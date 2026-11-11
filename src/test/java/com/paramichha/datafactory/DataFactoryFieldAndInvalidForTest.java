package com.paramichha.datafactory;

import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Hand-written cases for the two DataFactory methods that TestKit cannot write as literals:
 * {@code field(Field)} takes a reflection Field, and {@code invalidFor} names a field of a class.
 * The generated DataFactoryGeneratedTest leaves these out.
 */
@DisplayName("DataFactory field(Field) and invalidFor")
class DataFactoryFieldAndInvalidForTest {

    static class Shelf {
        @Size(min = 2, max = 4)
        String code;
        int rows;
    }

    private static Field codeField() throws NoSuchFieldException {
        return Shelf.class.getDeclaredField("code");
    }

    @Test
    @DisplayName("field(Field) gives values that satisfy the field's annotations")
    void fieldValuesSatisfyTheAnnotations() throws Exception {
        List<String> values = DataFactory.<String>field(codeField()).validList();
        assertThat(values).isNotEmpty()
                .allSatisfy(v -> assertThat(v.length()).isBetween(2, 4));
    }

    @Test
    @DisplayName("field(Field) gives invalid values that break the annotations")
    void fieldInvalidValuesBreakTheAnnotations() throws Exception {
        List<String> invalid = DataFactory.<String>field(codeField()).invalidList();
        assertThat(invalid).isNotEmpty()
                .allSatisfy(v -> assertThat(v == null || v.length() < 2 || v.length() > 4).isTrue());
    }

    @Test
    @DisplayName("field(Field) rejects a null field")
    void fieldRejectsNull() {
        assertThatThrownBy(() -> DataFactory.field((Field) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("invalidFor breaks the named field and leaves a payload")
    void invalidForBreaksTheNamedField() {
        DataFactoryResult<Shelf> result = DataFactory.invalidFor(Shelf.class, "code");
        assertThat(result.isSuccess()).isTrue();
        Shelf shelf = result.get();
        assertThat(shelf.code == null || shelf.code.length() < 2 || shelf.code.length() > 4).isTrue();
    }

    @Test
    @DisplayName("invalidFor for a field the class does not have is not a success")
    void invalidForUnknownFieldFails() {
        assertThat(DataFactory.invalidFor(Shelf.class, "missing").isSuccess()).isFalse();
    }
}
