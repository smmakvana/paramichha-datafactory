package com.paramichha.datafactory.regression;

import com.paramichha.datafactory.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valid values for collection fields from the field-level API: a List field's value is a list
 * (not its first element), a Queue's value is a Queue, and every valid collection respects @Size.
 */
@DisplayName("collections: valid values")
class CollectionValidValuesTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> valid(Class<?> type, String... annotations) {
        var field = DataFactory.field((Class) type);
        for (String a : annotations) field = field.with(a);
        return (List<Object>) field.seed(7L).validList();
    }

    @Test
    void listFieldValueIsAListNotItsElements() {
        assertThat(valid(List.class)).isNotEmpty().allSatisfy(v -> assertThat(v).isInstanceOf(List.class));
        assertThat(valid(Collection.class)).allSatisfy(v -> assertThat(v).isInstanceOf(Collection.class));
        Object single = DataFactory.field(List.class).seed(7L).valid();
        assertThat(single).isInstanceOf(List.class);
    }

    @Test
    void queueAndDequeKeepTheirType() {
        assertThat(valid(Queue.class)).isNotEmpty().allSatisfy(v -> assertThat(v).isInstanceOf(ArrayDeque.class));
        assertThat(valid(Deque.class)).isNotEmpty().allSatisfy(v -> assertThat(v).isInstanceOf(ArrayDeque.class));
    }

    @Test
    void validCollectionsNeverExceedSizeMax() {
        assertThat(valid(Set.class, "@Size(min=1, max=3)")).isNotEmpty()
                .allSatisfy(v -> assertThat(((Set<?>) v).size()).isBetween(1, 3));
        assertThat(valid(List.class, "@Size(min=2, max=4)")).isNotEmpty()
                .allSatisfy(v -> assertThat(((List<?>) v).size()).isBetween(2, 4));
    }

    enum Colour { RED, GREEN }

    @Test
    void enumsStillExpandToEveryConstant() {
        assertThat(valid(Colour.class)).containsExactly(Colour.RED, Colour.GREEN);
    }
}
