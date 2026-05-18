package com.paramichha.testkit;

import com.paramichha.datafactory.catalog.annotation.combined.CombinedRecord;
import com.paramichha.datafactory.catalog.annotation.combined.CombinedAllArgs;
import com.paramichha.datafactory.catalog.annotation.email.EmailRecord;
import com.paramichha.datafactory.catalog.annotation.numeric.NumericConstraintRecord;
import com.paramichha.datafactory.catalog.annotation.numeric.NumericConstraintAllArgs;
import com.paramichha.datafactory.catalog.number.NumberRecord;
import com.paramichha.datafactory.catalog.nested.NestedRecord;
import com.paramichha.datafactory.catalog.realworld.registration.UserRegistration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TestKit")
class TestKitTest {

    @Nested
    @DisplayName("of()")
    class OfMethod {

        @Test
        void nullTypeThrows() {
            assertThatThrownBy(() -> TestKits.of((Class<?>) null))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void returnsTestKitInstance() {
            assertThat(TestKits.of(UserRegistration.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("generate()")
    class GenerateMethod {

        @Test
        void generatesSourceForRecord() {
            String src = TestKits.of(UserRegistration.class)
                    .withPackage("com.example.test").generate().asString();
            assertThat(src).contains("package com.example.test");
            assertThat(src).contains("class UserRegistrationTest");
            assertThat(src).contains("UserRegistration userRegistration");
            assertThat(src).contains("UserRegistrationCreationTest");
        }

        @Test
        void generatesSourceForAllArgs() {
            String src = TestKits.of(CombinedAllArgs.class)
                    .withPackage("com.example.test").generate().asString();
            assertThat(src).contains("CombinedAllArgs combinedAllArgs");
            assertThat(src).contains("CombinedAllArgsCreationTest");
        }

        @Test
        void defaultPackageIsClassPackage() {
            GeneratedTestClass result = TestKits.of(UserRegistration.class).generate();
            assertThat(result.packageName())
                    .isEqualTo(UserRegistration.class.getPackageName());
        }

        @Test
        void classNameIsTypeNamePlusSuffix() {
            GeneratedTestClass result = TestKits.of(UserRegistration.class).generate();
            assertThat(result.className()).isEqualTo("UserRegistrationTest");
        }

        @Test
        void annotatedClassHasMemberNestedClasses() {
            String src = TestKits.of(NumericConstraintRecord.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).contains("@Nested");
            assertThat(src).contains("in NumericConstraintRecord");
        }

        @Test
        void annotatedClassHasValidator() {
            String src = TestKits.of(UserRegistration.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).contains("Validator validator");
            assertThat(src).contains("setUpValidator");
        }

        @Test
        void unannotatedClassHasNoValidator() {
            String src = TestKits.of(NumberRecord.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).doesNotContain("setUpValidator");
            assertThat(src).doesNotContain("Validation.buildDefaultValidatorFactory");
        }

        @Test
        void annotatedFieldHasRejectTest() {
            String src = TestKits.of(EmailRecord.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).contains("rejectsEmail");
        }

        @Test
        void unannotatedFieldRejectIsCommentedOut() {
            String src = TestKits.of(NumberRecord.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).contains("// TODO");
        }

        @Test
        void bracesAreBalanced() {
            String src = TestKits.of(CombinedRecord.class)
                    .withPackage("com.example").generate().asString();
            long opens  = src.chars().filter(c -> c == '{').count();
            long closes = src.chars().filter(c -> c == '}').count();
            assertThat(opens).isEqualTo(closes);
        }

        @Test
        void nestedObjectClassGenerates() {
            String src = TestKits.of(NestedRecord.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).contains("NestedRecordTest");
        }

        @Test
        void numericBoundaryValuesPresent() {
            String src = TestKits.of(NumericConstraintAllArgs.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).containsAnyOf("18", "19", "20");
        }
    }

    @Nested
    @DisplayName("withPackage()")
    class WithPackage {

        @Test
        void blankPackageThrows() {
            assertThatThrownBy(() -> TestKits.of(UserRegistration.class).withPackage(""))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void packageAppearsInOutput() {
            String src = TestKits.of(UserRegistration.class)
                    .withPackage("com.custom.pkg").generate().asString();
            assertThat(src).contains("package com.custom.pkg;");
        }
    }

    @Nested
    @DisplayName("GeneratedTestClass")
    class GeneratedTestClassTests {

        @Test
        void asStringReturnsSource() {
            GeneratedTestClass result = TestKits.of(UserRegistration.class).generate();
            assertThat(result.asString()).isEqualTo(result.source());
        }

        @Test
        void writeToCreatesFile(@TempDir Path tmp) {
            GeneratedTestClass result = TestKits.of(UserRegistration.class)
                    .withPackage("com.example.out").generate();
            Path written = result.writeTo(tmp);
            assertThat(written).exists();
            assertThat(written.getFileName().toString())
                    .isEqualTo("UserRegistrationTest.java");
        }
    }

    @Nested
    @DisplayName("split() — generateAll()")
    class SplitTests {

        @Test
        void generateAllProducesMultipleFiles() {
            var files = TestKits.of(UserRegistration.class)
                    .withPackage("com.example.split")
                    .split()
                    .generateAll();
            assertThat(files).hasSizeGreaterThan(1);
        }

        @Test
        void baseClassIsFirst() {
            var files = TestKits.of(UserRegistration.class)
                    .withPackage("com.example.split")
                    .split()
                    .generateAll();
            assertThat(files.get(0).className()).isEqualTo("UserRegistrationTest");
        }

        @Test
        void creationFileIsSecond() {
            var files = TestKits.of(UserRegistration.class)
                    .withPackage("com.example.split")
                    .split()
                    .generateAll();
            assertThat(files.get(1).className()).isEqualTo("UserRegistrationCreationTest");
        }

        @Test
        void allFilesHaveCorrectPackage() {
            var files = TestKits.of(UserRegistration.class)
                    .withPackage("com.example.split")
                    .split()
                    .generateAll();
            files.forEach(f ->
                    assertThat(f.packageName()).isEqualTo("com.example.split"));
        }

        @Test
        void bracesBalancedInAllFiles() {
            var files = TestKits.of(CombinedRecord.class)
                    .withPackage("com.example.split")
                    .split()
                    .generateAll();
            files.forEach(f -> {
                String src = f.asString();
                long opens  = src.chars().filter(c -> c == '{').count();
                long closes = src.chars().filter(c -> c == '}').count();
                assertThat(opens)
                        .as("unbalanced braces in %s", f.className())
                        .isEqualTo(closes);
            });
        }
    }
}