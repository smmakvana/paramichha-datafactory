package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.wrapper.bytew.WrapperByte;
import com.paramichha.testkit.fixture.wrapper.bytew.WrapperByteMany;
import com.paramichha.testkit.fixture.wrapper.shortw.WrapperShort;
import com.paramichha.testkit.fixture.wrapper.shortw.WrapperShortMany;
import com.paramichha.testkit.fixture.wrapper.integerw.WrapperInteger;
import com.paramichha.testkit.fixture.wrapper.integerw.WrapperIntegerMany;
import com.paramichha.testkit.fixture.wrapper.longw.WrapperLong;
import com.paramichha.testkit.fixture.wrapper.longw.WrapperLongMany;
import com.paramichha.testkit.fixture.wrapper.floatw.WrapperFloat;
import com.paramichha.testkit.fixture.wrapper.floatw.WrapperFloatMany;
import com.paramichha.testkit.fixture.wrapper.doublew.WrapperDouble;
import com.paramichha.testkit.fixture.wrapper.doublew.WrapperDoubleMany;
import com.paramichha.testkit.fixture.wrapper.booleanw.WrapperBoolean;
import com.paramichha.testkit.fixture.wrapper.booleanw.WrapperBooleanMany;
import com.paramichha.testkit.fixture.wrapper.bigdecimalw.WrapperBigDecimal;
import com.paramichha.testkit.fixture.wrapper.bigdecimalw.WrapperBigDecimalMany;
import com.paramichha.testkit.fixture.wrapper.bigintegerw.WrapperBigInteger;
import com.paramichha.testkit.fixture.wrapper.bigintegerw.WrapperBigIntegerMany;
import com.paramichha.testkit.fixture.wrapper.stringw.WrapperString;
import com.paramichha.testkit.fixture.wrapper.stringw.WrapperStringMany;
import com.paramichha.testkit.fixture.wrapper.uuidw.WrapperUuid;
import com.paramichha.testkit.fixture.wrapper.uuidw.WrapperUuidMany;
import com.paramichha.testkit.fixture.wrapper.characterw.WrapperCharacter;
import com.paramichha.testkit.fixture.wrapper.characterw.WrapperCharacterMany;
import com.paramichha.testkit.fixture.wrapper.enumw.WrapperEnum;
import com.paramichha.testkit.fixture.wrapper.enumw.WrapperEnumMany;

@DisplayName("Phase 2 — Wrapper types")
class WrapperPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            WrapperByte.class,
            WrapperByteMany.class,
            WrapperShort.class,
            WrapperShortMany.class,
            WrapperInteger.class,
            WrapperIntegerMany.class,
            WrapperLong.class,
            WrapperLongMany.class,
            WrapperFloat.class,
            WrapperFloatMany.class,
            WrapperDouble.class,
            WrapperDoubleMany.class,
            WrapperBoolean.class,
            WrapperBooleanMany.class,
            WrapperBigDecimal.class,
            WrapperBigDecimalMany.class,
            WrapperBigInteger.class,
            WrapperBigIntegerMany.class,
            WrapperString.class,
            WrapperStringMany.class,
            WrapperUuid.class,
            WrapperUuidMany.class,
            WrapperCharacter.class,
            WrapperCharacterMany.class,
            WrapperEnum.class,
            WrapperEnumMany.class
        ).writeTo(tmp);
        assertThat(written).hasSize(26);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(WrapperByte.class).generate().asString(), "WrapperByte");
        assertStructure(TestKit.of(WrapperByteMany.class).generate().asString(), "WrapperByteMany");
        assertStructure(TestKit.of(WrapperShort.class).generate().asString(), "WrapperShort");
        assertStructure(TestKit.of(WrapperShortMany.class).generate().asString(), "WrapperShortMany");
        assertStructure(TestKit.of(WrapperInteger.class).generate().asString(), "WrapperInteger");
        assertStructure(TestKit.of(WrapperIntegerMany.class).generate().asString(), "WrapperIntegerMany");
        assertStructure(TestKit.of(WrapperLong.class).generate().asString(), "WrapperLong");
        assertStructure(TestKit.of(WrapperLongMany.class).generate().asString(), "WrapperLongMany");
        assertStructure(TestKit.of(WrapperFloat.class).generate().asString(), "WrapperFloat");
        assertStructure(TestKit.of(WrapperFloatMany.class).generate().asString(), "WrapperFloatMany");
        assertStructure(TestKit.of(WrapperDouble.class).generate().asString(), "WrapperDouble");
        assertStructure(TestKit.of(WrapperDoubleMany.class).generate().asString(), "WrapperDoubleMany");
        assertStructure(TestKit.of(WrapperBoolean.class).generate().asString(), "WrapperBoolean");
        assertStructure(TestKit.of(WrapperBooleanMany.class).generate().asString(), "WrapperBooleanMany");
        assertStructure(TestKit.of(WrapperBigDecimal.class).generate().asString(), "WrapperBigDecimal");
        assertStructure(TestKit.of(WrapperBigDecimalMany.class).generate().asString(), "WrapperBigDecimalMany");
        assertStructure(TestKit.of(WrapperBigInteger.class).generate().asString(), "WrapperBigInteger");
        assertStructure(TestKit.of(WrapperBigIntegerMany.class).generate().asString(), "WrapperBigIntegerMany");
        assertStructure(TestKit.of(WrapperString.class).generate().asString(), "WrapperString");
        assertStructure(TestKit.of(WrapperStringMany.class).generate().asString(), "WrapperStringMany");
        assertStructure(TestKit.of(WrapperUuid.class).generate().asString(), "WrapperUuid");
        assertStructure(TestKit.of(WrapperUuidMany.class).generate().asString(), "WrapperUuidMany");
        assertStructure(TestKit.of(WrapperCharacter.class).generate().asString(), "WrapperCharacter");
        assertStructure(TestKit.of(WrapperCharacterMany.class).generate().asString(), "WrapperCharacterMany");
        assertStructure(TestKit.of(WrapperEnum.class).generate().asString(), "WrapperEnum");
        assertStructure(TestKit.of(WrapperEnumMany.class).generate().asString(), "WrapperEnumMany");
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
