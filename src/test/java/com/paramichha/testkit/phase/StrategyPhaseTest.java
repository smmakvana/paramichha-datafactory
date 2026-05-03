package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.strategy.record.StrategyRecord;
import com.paramichha.testkit.fixture.strategy.record.StrategyRecordAnnotated;
import com.paramichha.testkit.fixture.strategy.lombok.StrategyLombokBuilder;
import com.paramichha.testkit.fixture.strategy.lombok.StrategyLombokBuilderAnnotated;
import com.paramichha.testkit.fixture.strategy.allargs.StrategyAllArgs;
import com.paramichha.testkit.fixture.strategy.allargs.StrategyAllArgsAnnotated;
import com.paramichha.testkit.fixture.strategy.setter.StrategySetter;
import com.paramichha.testkit.fixture.strategy.setter.StrategySetterAnnotated;

@DisplayName("Phase 10 — Instantiation strategies")
class StrategyPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            StrategyRecord.class,
            StrategyRecordAnnotated.class,
            StrategyLombokBuilder.class,
            StrategyLombokBuilderAnnotated.class,
            StrategyAllArgs.class,
            StrategyAllArgsAnnotated.class,
            StrategySetter.class,
            StrategySetterAnnotated.class
        ).writeTo(tmp);
        assertThat(written).hasSize(8);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(StrategyRecord.class).generate().asString(), "StrategyRecord");
        assertStructure(TestKit.of(StrategyRecordAnnotated.class).generate().asString(), "StrategyRecordAnnotated");
        assertStructure(TestKit.of(StrategyLombokBuilder.class).generate().asString(), "StrategyLombokBuilder");
        assertStructure(TestKit.of(StrategyLombokBuilderAnnotated.class).generate().asString(), "StrategyLombokBuilderAnnotated");
        assertStructure(TestKit.of(StrategyAllArgs.class).generate().asString(), "StrategyAllArgs");
        assertStructure(TestKit.of(StrategyAllArgsAnnotated.class).generate().asString(), "StrategyAllArgsAnnotated");
        assertStructure(TestKit.of(StrategySetter.class).generate().asString(), "StrategySetter");
        assertStructure(TestKit.of(StrategySetterAnnotated.class).generate().asString(), "StrategySetterAnnotated");
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
