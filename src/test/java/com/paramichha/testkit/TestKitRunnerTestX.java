package com.paramichha.testkit;

import com.paramichha.datafactory.catalog.annotation.combined.CombinedRecord;
import com.paramichha.datafactory.catalog.annotation.combined.CombinedAllArgs;
import com.paramichha.datafactory.catalog.annotation.email.EmailRecord;
import com.paramichha.datafactory.catalog.annotation.numeric.NumericConstraintRecord;
import com.paramichha.datafactory.catalog.annotation.notnull.NotNullRecord;
import com.paramichha.datafactory.catalog.annotation.notnull.NotNullAllArgs;
import com.paramichha.datafactory.catalog.date.DateRecord;
import com.paramichha.datafactory.catalog.decimal.DecimalRecord;
import com.paramichha.datafactory.catalog.nested.NestedRecord;
import com.paramichha.datafactory.catalog.number.NumberRecord;
import com.paramichha.datafactory.catalog.realworld.payment.Payment;
import com.paramichha.datafactory.catalog.realworld.registration.UserRegistration;
import com.paramichha.datafactory.catalog.realworld.trading.Trade;
import com.paramichha.testkit.runner.TestKitRunner;
import com.paramichha.testkit.utils.JavaNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TestKit + TestKitRunner")
class TestKitRunnerTestX {

    // ── single class generation ───────────────────────────────────────────

    @Nested
    @DisplayName("TestKit.of() — single class")
    class SingleClass {

        @Test
        void nullTypeThrows() {
            assertThatThrownBy(() -> TestKitsX.of((Class<?>) null))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void blankPackageThrows() {
            assertThatThrownBy(() -> TestKitsX.of(UserRegistration.class).withPackage(""))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void generatesRecord() {
            String src = TestKitsX.of(UserRegistration.class).generateTest().asString();
            assertStructure(src, "UserRegistration");
            assertHasValidator(src);
        }

        @Test
        void generatesAllArgs() {
            String src = TestKitsX.of(NotNullAllArgs.class).generateTest().asString();
            assertStructure(src, "NotNullAllArgs");
            assertHasValidator(src);
        }

        @Test
        void generatesTemporal() {
            String src = TestKitsX.of(DateRecord.class).generateTest().asString();
            assertStructure(src, "DateRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesNested() {
            String src = TestKitsX.of(NestedRecord.class).generateTest().asString();
            assertStructure(src, "NestedRecord");
        }

        @Test
        void generatesUnannotated() {
            String src = TestKitsX.of(NumberRecord.class).generateTest().asString();
            assertStructure(src, "NumberRecord");
            assertNoValidator(src);
        }

        @Test
        void generatesSingleAnnotation() {
            String src = TestKitsX.of(NotNullRecord.class).generateTest().asString();
            assertStructure(src, "NotNullRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesCombinedAnnotation() {
            String src = TestKitsX.of(NumericConstraintRecord.class).generateTest().asString();
            assertStructure(src, "NumericConstraintRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesEmail() {
            String src = TestKitsX.of(EmailRecord.class).generateTest().asString();
            assertStructure(src, "EmailRecord");
            assertThat(src).contains("notanemail");
        }

        @Test
        void generatesCombined() {
            String src = TestKitsX.of(CombinedRecord.class).generateTest().asString();
            assertStructure(src, "CombinedRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesPayment() {
            String src = TestKitsX.of(Payment.class).generateTest().asString();
            assertStructure(src, "Payment");
            assertThat(src).contains("new Payment(");
        }

        @Test
        void generatesTrade() {
            String src = TestKitsX.of(Trade.class).generateTest().asString();
            assertStructure(src, "Trade");
        }

        @Test
        void defaultPackageMatchesClass() {
            GenerationResult result = TestKitsX.of(UserRegistration.class).generateTest();
            assertThat(result.packageName())
                    .isEqualTo(UserRegistration.class.getPackageName());
        }

        @Test
        void classNameHasTestSuffix() {
            GenerationResult result = TestKitsX.of(UserRegistration.class).generateTest();
            assertThat(result.className()).isEqualTo("UserRegistrationTest");
        }

        @Test
        void bracesBalanced() {
            for (Class<?> c : coreFixtures()) {
                String src = TestKitsX.of(c).generateTest().asString();
                long opens  = src.chars().filter(ch -> ch == '{').count();
                long closes = src.chars().filter(ch -> ch == '}').count();
                assertThat(opens)
                        .as("unbalanced braces in %s", c.getSimpleName())
                        .isEqualTo(closes);
            }
        }

        @Test
        void writesFileToCorrectPath(@TempDir Path tmp) {
            Path written = TestKitsX.of(UserRegistration.class)
                    .withPackage("com.example.out")
                    .generateTest()
                    .writeTo(tmp);
            assertThat(written).exists();
            assertThat(written.getFileName().toString())
                    .isEqualTo("UserRegistrationTest.java");
            assertThat(written.getParent().toString()).endsWith("out");
        }
    }

    // ── batch runner ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("TestKitRunner")
    class Runner {

        @Test
        void forClassesGeneratesAll(@TempDir Path tmp) {
            List<Path> written = TestKitRunner
                    .forClasses(UserRegistration.class, Payment.class, CombinedAllArgs.class)
                    .writeTo(tmp);
            assertThat(written).hasSize(3);
            written.forEach(p -> assertThat(p).exists());
        }

        @Test
        void eachFileEndsWithTestSuffix(@TempDir Path tmp) {
            TestKitRunner.forClasses(coreFixtures().toArray(Class[]::new))
                    .writeTo(tmp)
                    .forEach(p -> assertThat(p.getFileName().toString())
                            .endsWith("Test.java"));
        }

        @Test
        void targetCountMatchesInput() {
            assertThat(TestKitRunner
                    .forClasses(coreFixtures().toArray(Class[]::new))
                    .targetCount())
                    .isEqualTo(coreFixtures().size());
        }

        @Test
        void scanPackageFindsTestableClasses() {
            List<GenerationResult> results = TestKitRunner
                    .scanPackage("com.paramichha.datafactory.catalog.realworld")
                    .generate();
            assertThat(results).isNotEmpty();
            results.forEach(g -> assertThat(g.className()).endsWith("Test"));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static void assertStructure(String src, String className) {
        assertThat(src).contains("class " + className + "Test");
        assertThat(src).contains(className + "CreationTest");
        assertThat(src).contains(JavaNames.variableName(className));
        assertThat(src).contains("testCanBeCreated");
        assertThat(src).contains("testHoldsExpectedValues");
    }

    private static void assertHasValidator(String src) {
        assertThat(src).contains("Validator validator");
        assertThat(src).contains("setUpValidator");
        assertThat(src).contains("testIsValid");
    }

    private static void assertNoValidator(String src) {
        assertThat(src).doesNotContain("setUpValidator");
        assertThat(src).doesNotContain("Validation.buildDefaultValidatorFactory");
    }

    private static List<Class<?>> coreFixtures() {
        return List.of(
                NumberRecord.class,
                DecimalRecord.class,
                DateRecord.class,
                NotNullRecord.class,
                NumericConstraintRecord.class,
                CombinedRecord.class,
                CombinedAllArgs.class,
                NestedRecord.class,
                UserRegistration.class,
                Payment.class,
                Trade.class
        );
    }
}