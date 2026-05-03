package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.nested.onelevel.NestedOneLevel;
import com.paramichha.testkit.fixture.nested.onelevel.NestedOneLevelMany;
import com.paramichha.testkit.fixture.nested.onelevel.NestedOneLevelNullable;
import com.paramichha.testkit.fixture.nested.twolevel.NestedTwoLevel;
import com.paramichha.testkit.fixture.nested.twolevel.NestedTwoLevelCollection;

@DisplayName("Phase 11 — Nested objects")
class NestedPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            NestedOneLevel.class,
            NestedOneLevelMany.class,
            NestedOneLevelNullable.class,
            NestedTwoLevel.class,
            NestedTwoLevelCollection.class
        ).writeTo(tmp);
        assertThat(written).hasSize(5);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(NestedOneLevel.class).generate().asString(), "NestedOneLevel");
        assertStructure(TestKit.of(NestedOneLevelMany.class).generate().asString(), "NestedOneLevelMany");
        assertStructure(TestKit.of(NestedOneLevelNullable.class).generate().asString(), "NestedOneLevelNullable");
        assertStructure(TestKit.of(NestedTwoLevel.class).generate().asString(), "NestedTwoLevel");
        assertStructure(TestKit.of(NestedTwoLevelCollection.class).generate().asString(), "NestedTwoLevelCollection");
    }

    private static void assertStructure(String src, String className) {
        // Accept either a full generated test or a placeholder comment file
        boolean isFull = src.contains("class " + className + "Test")
                && src.contains("class SmokeTest");
        boolean isPlaceholder = src.contains("TODO: " + className + "Test");
        assertThat(isFull || isPlaceholder)
                .as("Expected full test or placeholder for %s", className)
                .isTrue();
    }
}
