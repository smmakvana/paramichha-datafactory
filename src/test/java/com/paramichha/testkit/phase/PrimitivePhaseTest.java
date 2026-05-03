package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.primitive.bytep.PrimitiveByte;
import com.paramichha.testkit.fixture.primitive.bytep.PrimitiveByteMany;
import com.paramichha.testkit.fixture.primitive.shortp.PrimitiveShort;
import com.paramichha.testkit.fixture.primitive.shortp.PrimitiveShortMany;
import com.paramichha.testkit.fixture.primitive.intp.PrimitiveInt;
import com.paramichha.testkit.fixture.primitive.intp.PrimitiveIntMany;
import com.paramichha.testkit.fixture.primitive.longp.PrimitiveLong;
import com.paramichha.testkit.fixture.primitive.longp.PrimitiveLongMany;
import com.paramichha.testkit.fixture.primitive.floatp.PrimitiveFloat;
import com.paramichha.testkit.fixture.primitive.floatp.PrimitiveFloatMany;
import com.paramichha.testkit.fixture.primitive.doublep.PrimitiveDouble;
import com.paramichha.testkit.fixture.primitive.doublep.PrimitiveDoubleMany;
import com.paramichha.testkit.fixture.primitive.booleanp.PrimitiveBoolean;
import com.paramichha.testkit.fixture.primitive.booleanp.PrimitiveBooleanMany;
import com.paramichha.testkit.fixture.primitive.charp.PrimitiveChar;
import com.paramichha.testkit.fixture.primitive.charp.PrimitiveCharMany;

@DisplayName("Phase 1 — Primitive types")
class PrimitivePhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            PrimitiveByte.class,
            PrimitiveByteMany.class,
            PrimitiveShort.class,
            PrimitiveShortMany.class,
            PrimitiveInt.class,
            PrimitiveIntMany.class,
            PrimitiveLong.class,
            PrimitiveLongMany.class,
            PrimitiveFloat.class,
            PrimitiveFloatMany.class,
            PrimitiveDouble.class,
            PrimitiveDoubleMany.class,
            PrimitiveBoolean.class,
            PrimitiveBooleanMany.class,
            PrimitiveChar.class,
            PrimitiveCharMany.class
        ).writeTo(tmp);
        assertThat(written).hasSize(16);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(PrimitiveByte.class).generate().asString(), "PrimitiveByte");
        assertStructure(TestKit.of(PrimitiveByteMany.class).generate().asString(), "PrimitiveByteMany");
        assertStructure(TestKit.of(PrimitiveShort.class).generate().asString(), "PrimitiveShort");
        assertStructure(TestKit.of(PrimitiveShortMany.class).generate().asString(), "PrimitiveShortMany");
        assertStructure(TestKit.of(PrimitiveInt.class).generate().asString(), "PrimitiveInt");
        assertStructure(TestKit.of(PrimitiveIntMany.class).generate().asString(), "PrimitiveIntMany");
        assertStructure(TestKit.of(PrimitiveLong.class).generate().asString(), "PrimitiveLong");
        assertStructure(TestKit.of(PrimitiveLongMany.class).generate().asString(), "PrimitiveLongMany");
        assertStructure(TestKit.of(PrimitiveFloat.class).generate().asString(), "PrimitiveFloat");
        assertStructure(TestKit.of(PrimitiveFloatMany.class).generate().asString(), "PrimitiveFloatMany");
        assertStructure(TestKit.of(PrimitiveDouble.class).generate().asString(), "PrimitiveDouble");
        assertStructure(TestKit.of(PrimitiveDoubleMany.class).generate().asString(), "PrimitiveDoubleMany");
        assertStructure(TestKit.of(PrimitiveBoolean.class).generate().asString(), "PrimitiveBoolean");
        assertStructure(TestKit.of(PrimitiveBooleanMany.class).generate().asString(), "PrimitiveBooleanMany");
        assertStructure(TestKit.of(PrimitiveChar.class).generate().asString(), "PrimitiveChar");
        assertStructure(TestKit.of(PrimitiveCharMany.class).generate().asString(), "PrimitiveCharMany");
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
