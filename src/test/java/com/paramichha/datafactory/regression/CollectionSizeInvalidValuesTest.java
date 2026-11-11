package com.paramichha.datafactory.regression;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.DataFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @Size on a collection or map counts elements, and Bean Validation treats null as valid for
 * @Size (only @NotNull rejects it). The invalid values must be one element too few and one too
 * many, never null. TestKit uses these values for its "rejects invalid values" tests.
 */
@DisplayName("@Size on collections and maps: invalid values")
class CollectionSizeInvalidValuesTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> invalid(Class<?> type, String size) {
        return (List<Object>)
                DataFactory.field((Class) type).with(size).seed(7L).invalidList();
    }

    private static List<Integer> sizes(List<Object> values) {
        return values.stream()
                .map(v -> v instanceof Map<?, ?> m ? m.size() : ((Collection<?>) v).size())
                .toList();
    }

    @Test
    void listGetsOneTooFewAndOneTooManyAndNoNull() {
        List<Object> values = invalid(List.class, "@Size(min=1, max=5)");
        assertThat(values).doesNotContainNull().allSatisfy(v -> assertThat(v).isInstanceOf(List.class));
        assertThat(sizes(values)).containsExactly(0, 6);
    }

    @Test
    void setElementsAreDistinctSoTheSizeHolds() {
        List<Object> values = invalid(Set.class, "@Size(min=2, max=3)");
        assertThat(values).doesNotContainNull().allSatisfy(v -> assertThat(v).isInstanceOf(Set.class));
        assertThat(sizes(values)).containsExactly(1, 4);
    }

    @Test
    void queueKeepsItsType() {
        List<Object> values = invalid(Queue.class, "@Size(min=1, max=2)");
        assertThat(values).doesNotContainNull().allSatisfy(v -> assertThat(v).isInstanceOf(Queue.class));
        assertThat(sizes(values)).containsExactly(0, 3);
    }

    @Test
    void mapCountsEntries() {
        List<Object> values = invalid(Map.class, "@Size(min=1, max=10)");
        assertThat(values).doesNotContainNull().allSatisfy(v -> assertThat(v).isInstanceOf(Map.class));
        assertThat(sizes(values)).containsExactly(0, 11);
    }

    @Test
    void zeroMinimumHasNoTooFewCase() {
        assertThat(sizes(invalid(List.class, "@Size(max=2)"))).containsExactly(3);
    }

    @Test
    void hugeMaximumHasNoTooManyCase() {
        assertThat(sizes(invalid(List.class, "@Size(min=1, max=100000)"))).containsExactly(0);
    }

    @Test
    void stringsAreUnchanged() {
        List<Object> values = invalid(String.class, "@Size(min=2, max=3)");
        assertThat(values).contains("x", "xxxx");
    }
}
