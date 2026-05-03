package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.mixed.MixedPrimitiveString;
import com.paramichha.testkit.fixture.mixed.MixedPrimitiveWrapper;
import com.paramichha.testkit.fixture.mixed.MixedPrimitiveTemporal;
import com.paramichha.testkit.fixture.mixed.MixedWrapperString;
import com.paramichha.testkit.fixture.mixed.MixedWrapperTemporal;
import com.paramichha.testkit.fixture.mixed.MixedStringTemporal;
import com.paramichha.testkit.fixture.mixed.MixedStringEnum;
import com.paramichha.testkit.fixture.mixed.MixedAllFlat;
import com.paramichha.testkit.fixture.mixed.MixedAllAnnotated;

@DisplayName("Phase 9 — Mixed types")
class MixedPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            MixedPrimitiveString.class,
            MixedPrimitiveWrapper.class,
            MixedPrimitiveTemporal.class,
            MixedWrapperString.class,
            MixedWrapperTemporal.class,
            MixedStringTemporal.class,
            MixedStringEnum.class,
            MixedAllFlat.class,
            MixedAllAnnotated.class
        ).writeTo(tmp);
        assertThat(written).hasSize(9);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(MixedPrimitiveString.class).generate().asString(), "MixedPrimitiveString");
        assertStructure(TestKit.of(MixedPrimitiveWrapper.class).generate().asString(), "MixedPrimitiveWrapper");
        assertStructure(TestKit.of(MixedPrimitiveTemporal.class).generate().asString(), "MixedPrimitiveTemporal");
        assertStructure(TestKit.of(MixedWrapperString.class).generate().asString(), "MixedWrapperString");
        assertStructure(TestKit.of(MixedWrapperTemporal.class).generate().asString(), "MixedWrapperTemporal");
        assertStructure(TestKit.of(MixedStringTemporal.class).generate().asString(), "MixedStringTemporal");
        assertStructure(TestKit.of(MixedStringEnum.class).generate().asString(), "MixedStringEnum");
        assertStructure(TestKit.of(MixedAllFlat.class).generate().asString(), "MixedAllFlat");
        assertStructure(TestKit.of(MixedAllAnnotated.class).generate().asString(), "MixedAllAnnotated");
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
