package com.paramichha.datafactory.regression;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.DataFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Regression: a type that contains itself through an Optional or a collection
 * (a tree, a reply thread) recursed until StackOverflowError. Like a plain
 * self-reference, it now stops one level down.
 */
class RecursiveTypesRegressionTest {

    record Category(String name, List<Category> children) {}

    record Reply(String text, Optional<Reply> parent) {}

    record Tag(String label, Set<Tag> related) {}

    record Folder(String name, Map<String, Folder> subfolders) {}

    @Test
    void aTreeHasOneLevelOfChildren() {
        Category root = DataFactory.of(Category.class).valid().get();
        assertThat(root.children())
                .singleElement()
                .satisfies(child -> assertThat(child.children()).isEmpty());
    }

    @Test
    void anOptionalSelfReferenceIsEmptyOneLevelDown() {
        Reply reply = DataFactory.of(Reply.class).valid().get();
        assertThat(reply.parent())
                .hasValueSatisfying(p -> assertThat(p.parent()).isEmpty());
    }

    @Test
    void setsAndMapsOfTheSameTypeStopToo() {
        assertThat(DataFactory.of(Tag.class).valid().isSuccess()).isTrue();
        assertThat(DataFactory.of(Folder.class).valid().isSuccess()).isTrue();
    }

    @Test
    void invalidCasesAndFieldsWorkForATree() {
        assertThat(DataFactory.of(Category.class).invalidList().total()).isPositive();
        assertThat(DataFactory.of(Category.class).fields()).containsKeys("name", "children");
    }
}
