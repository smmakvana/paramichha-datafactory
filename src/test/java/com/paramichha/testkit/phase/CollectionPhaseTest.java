package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.collection.liststring.CollectionListString;
import com.paramichha.testkit.fixture.collection.liststring.CollectionListStringNotNull;
import com.paramichha.testkit.fixture.collection.liststring.CollectionListStringNotEmpty;
import com.paramichha.testkit.fixture.collection.liststring.CollectionListStringSize;
import com.paramichha.testkit.fixture.collection.listinteger.CollectionListInteger;
import com.paramichha.testkit.fixture.collection.listinteger.CollectionListIntegerNotNull;
import com.paramichha.testkit.fixture.collection.listbigdecimal.CollectionListBigDecimal;
import com.paramichha.testkit.fixture.collection.listtemporal.CollectionListLocalDate;
import com.paramichha.testkit.fixture.collection.listuuid.CollectionListUuid;
import com.paramichha.testkit.fixture.collection.listenum.CollectionListEnum;
import com.paramichha.testkit.fixture.collection.colstring.CollectionOfString;
import com.paramichha.testkit.fixture.collection.listpojo.CollectionListPojo;
import com.paramichha.testkit.fixture.collection.listpojo.CollectionListPojoNotNull;

@DisplayName("Phase 8 — Collections")
class CollectionPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            CollectionListString.class,
            CollectionListStringNotNull.class,
            CollectionListStringNotEmpty.class,
            CollectionListStringSize.class,
            CollectionListInteger.class,
            CollectionListIntegerNotNull.class,
            CollectionListBigDecimal.class,
            CollectionListLocalDate.class,
            CollectionListUuid.class,
            CollectionListEnum.class,
            CollectionOfString.class,
            CollectionListPojo.class,
            CollectionListPojoNotNull.class
        ).writeTo(tmp);
        assertThat(written).hasSize(13);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(CollectionListString.class).generate().asString(), "CollectionListString");
        assertStructure(TestKit.of(CollectionListStringNotNull.class).generate().asString(), "CollectionListStringNotNull");
        assertStructure(TestKit.of(CollectionListStringNotEmpty.class).generate().asString(), "CollectionListStringNotEmpty");
        assertStructure(TestKit.of(CollectionListStringSize.class).generate().asString(), "CollectionListStringSize");
        assertStructure(TestKit.of(CollectionListInteger.class).generate().asString(), "CollectionListInteger");
        assertStructure(TestKit.of(CollectionListIntegerNotNull.class).generate().asString(), "CollectionListIntegerNotNull");
        assertStructure(TestKit.of(CollectionListBigDecimal.class).generate().asString(), "CollectionListBigDecimal");
        assertStructure(TestKit.of(CollectionListLocalDate.class).generate().asString(), "CollectionListLocalDate");
        assertStructure(TestKit.of(CollectionListUuid.class).generate().asString(), "CollectionListUuid");
        assertStructure(TestKit.of(CollectionListEnum.class).generate().asString(), "CollectionListEnum");
        assertStructure(TestKit.of(CollectionOfString.class).generate().asString(), "CollectionOfString");
        assertStructure(TestKit.of(CollectionListPojo.class).generate().asString(), "CollectionListPojo");
        assertStructure(TestKit.of(CollectionListPojoNotNull.class).generate().asString(), "CollectionListPojoNotNull");
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
