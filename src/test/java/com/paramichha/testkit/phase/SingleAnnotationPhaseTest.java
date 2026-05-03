package com.paramichha.testkit.phase;

import com.paramichha.testkit.TestKit;
import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullByte;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullShort;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullInteger;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullLong;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullFloat;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullDouble;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullBoolean;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullBigDecimal;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullBigInteger;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullString;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullUuid;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullLocalDate;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullLocalDateTime;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullEnum;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullMany;
import com.paramichha.testkit.fixture.annotation.notblank.AnnotationNotBlankString;
import com.paramichha.testkit.fixture.annotation.notblank.AnnotationNotBlankMany;
import com.paramichha.testkit.fixture.annotation.notempty.AnnotationNotEmptyString;
import com.paramichha.testkit.fixture.annotation.notempty.AnnotationNotEmptyList;
import com.paramichha.testkit.fixture.annotation.notempty.AnnotationNotEmptyMany;
import com.paramichha.testkit.fixture.annotation.nullconstraint.AnnotationNullString;
import com.paramichha.testkit.fixture.annotation.nullconstraint.AnnotationNullInteger;
import com.paramichha.testkit.fixture.annotation.email.AnnotationEmailString;
import com.paramichha.testkit.fixture.annotation.email.AnnotationEmailMany;
import com.paramichha.testkit.fixture.annotation.url.AnnotationUrlString;
import com.paramichha.testkit.fixture.annotation.url.AnnotationUrlMany;
import com.paramichha.testkit.fixture.annotation.creditcard.AnnotationCreditCardString;
import com.paramichha.testkit.fixture.annotation.isbn.AnnotationIsbnString;
import com.paramichha.testkit.fixture.annotation.ean.AnnotationEanString;
import com.paramichha.testkit.fixture.annotation.uuidconstraint.AnnotationUuidString;
import com.paramichha.testkit.fixture.annotation.pattern.AnnotationPatternString;
import com.paramichha.testkit.fixture.annotation.pattern.AnnotationPatternStringMany;
import com.paramichha.testkit.fixture.annotation.size.AnnotationSizeString;
import com.paramichha.testkit.fixture.annotation.size.AnnotationSizeStringMany;
import com.paramichha.testkit.fixture.annotation.size.AnnotationSizeList;
import com.paramichha.testkit.fixture.annotation.size.AnnotationSizeListMany;
import com.paramichha.testkit.fixture.annotation.length.AnnotationLengthString;
import com.paramichha.testkit.fixture.annotation.length.AnnotationLengthStringMany;

@DisplayName("Phase 4 — Single annotations")
class SingleAnnotationPhaseTest {

    @Test
    void all_generate_without_error(@TempDir Path tmp) {
        List<Path> written = TestKitRunner.forClasses(
            AnnotationNotNullByte.class,
            AnnotationNotNullShort.class,
            AnnotationNotNullInteger.class,
            AnnotationNotNullLong.class,
            AnnotationNotNullFloat.class,
            AnnotationNotNullDouble.class,
            AnnotationNotNullBoolean.class,
            AnnotationNotNullBigDecimal.class,
            AnnotationNotNullBigInteger.class,
            AnnotationNotNullString.class,
            AnnotationNotNullUuid.class,
            AnnotationNotNullLocalDate.class,
            AnnotationNotNullLocalDateTime.class,
            AnnotationNotNullEnum.class,
            AnnotationNotNullMany.class,
            AnnotationNotBlankString.class,
            AnnotationNotBlankMany.class,
            AnnotationNotEmptyString.class,
            AnnotationNotEmptyList.class,
            AnnotationNotEmptyMany.class,
            AnnotationNullString.class,
            AnnotationNullInteger.class,
            AnnotationEmailString.class,
            AnnotationEmailMany.class,
            AnnotationUrlString.class,
            AnnotationUrlMany.class,
            AnnotationCreditCardString.class,
            AnnotationIsbnString.class,
            AnnotationEanString.class,
            AnnotationUuidString.class,
            AnnotationPatternString.class,
            AnnotationPatternStringMany.class,
            AnnotationSizeString.class,
            AnnotationSizeStringMany.class,
            AnnotationSizeList.class,
            AnnotationSizeListMany.class,
            AnnotationLengthString.class,
            AnnotationLengthStringMany.class
        ).writeTo(tmp);
        assertThat(written).hasSize(38);
        written.forEach(p -> assertThat(p).exists());
    }

    @Test
    void all_have_correct_structure_or_placeholder() {
        assertStructure(TestKit.of(AnnotationNotNullByte.class).generate().asString(), "AnnotationNotNullByte");
        assertStructure(TestKit.of(AnnotationNotNullShort.class).generate().asString(), "AnnotationNotNullShort");
        assertStructure(TestKit.of(AnnotationNotNullInteger.class).generate().asString(), "AnnotationNotNullInteger");
        assertStructure(TestKit.of(AnnotationNotNullLong.class).generate().asString(), "AnnotationNotNullLong");
        assertStructure(TestKit.of(AnnotationNotNullFloat.class).generate().asString(), "AnnotationNotNullFloat");
        assertStructure(TestKit.of(AnnotationNotNullDouble.class).generate().asString(), "AnnotationNotNullDouble");
        assertStructure(TestKit.of(AnnotationNotNullBoolean.class).generate().asString(), "AnnotationNotNullBoolean");
        assertStructure(TestKit.of(AnnotationNotNullBigDecimal.class).generate().asString(), "AnnotationNotNullBigDecimal");
        assertStructure(TestKit.of(AnnotationNotNullBigInteger.class).generate().asString(), "AnnotationNotNullBigInteger");
        assertStructure(TestKit.of(AnnotationNotNullString.class).generate().asString(), "AnnotationNotNullString");
        assertStructure(TestKit.of(AnnotationNotNullUuid.class).generate().asString(), "AnnotationNotNullUuid");
        assertStructure(TestKit.of(AnnotationNotNullLocalDate.class).generate().asString(), "AnnotationNotNullLocalDate");
        assertStructure(TestKit.of(AnnotationNotNullLocalDateTime.class).generate().asString(), "AnnotationNotNullLocalDateTime");
        assertStructure(TestKit.of(AnnotationNotNullEnum.class).generate().asString(), "AnnotationNotNullEnum");
        assertStructure(TestKit.of(AnnotationNotNullMany.class).generate().asString(), "AnnotationNotNullMany");
        assertStructure(TestKit.of(AnnotationNotBlankString.class).generate().asString(), "AnnotationNotBlankString");
        assertStructure(TestKit.of(AnnotationNotBlankMany.class).generate().asString(), "AnnotationNotBlankMany");
        assertStructure(TestKit.of(AnnotationNotEmptyString.class).generate().asString(), "AnnotationNotEmptyString");
        assertStructure(TestKit.of(AnnotationNotEmptyList.class).generate().asString(), "AnnotationNotEmptyList");
        assertStructure(TestKit.of(AnnotationNotEmptyMany.class).generate().asString(), "AnnotationNotEmptyMany");
        assertStructure(TestKit.of(AnnotationNullString.class).generate().asString(), "AnnotationNullString");
        assertStructure(TestKit.of(AnnotationNullInteger.class).generate().asString(), "AnnotationNullInteger");
        assertStructure(TestKit.of(AnnotationEmailString.class).generate().asString(), "AnnotationEmailString");
        assertStructure(TestKit.of(AnnotationEmailMany.class).generate().asString(), "AnnotationEmailMany");
        assertStructure(TestKit.of(AnnotationUrlString.class).generate().asString(), "AnnotationUrlString");
        assertStructure(TestKit.of(AnnotationUrlMany.class).generate().asString(), "AnnotationUrlMany");
        assertStructure(TestKit.of(AnnotationCreditCardString.class).generate().asString(), "AnnotationCreditCardString");
        assertStructure(TestKit.of(AnnotationIsbnString.class).generate().asString(), "AnnotationIsbnString");
        assertStructure(TestKit.of(AnnotationEanString.class).generate().asString(), "AnnotationEanString");
        assertStructure(TestKit.of(AnnotationUuidString.class).generate().asString(), "AnnotationUuidString");
        assertStructure(TestKit.of(AnnotationPatternString.class).generate().asString(), "AnnotationPatternString");
        assertStructure(TestKit.of(AnnotationPatternStringMany.class).generate().asString(), "AnnotationPatternStringMany");
        assertStructure(TestKit.of(AnnotationSizeString.class).generate().asString(), "AnnotationSizeString");
        assertStructure(TestKit.of(AnnotationSizeStringMany.class).generate().asString(), "AnnotationSizeStringMany");
        assertStructure(TestKit.of(AnnotationSizeList.class).generate().asString(), "AnnotationSizeList");
        assertStructure(TestKit.of(AnnotationSizeListMany.class).generate().asString(), "AnnotationSizeListMany");
        assertStructure(TestKit.of(AnnotationLengthString.class).generate().asString(), "AnnotationLengthString");
        assertStructure(TestKit.of(AnnotationLengthStringMany.class).generate().asString(), "AnnotationLengthStringMany");
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
