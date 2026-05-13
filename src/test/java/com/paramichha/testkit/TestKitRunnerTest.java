package com.paramichha.testkit;

import com.paramichha.example.annotation.combined.CombinedRecord;
import com.paramichha.example.annotation.combined.CombinedAllArgs;
import com.paramichha.example.annotation.email.EmailRecord;
import com.paramichha.example.annotation.numeric.NumericConstraintRecord;
import com.paramichha.example.annotation.notnull.NotNullRecord;
import com.paramichha.example.annotation.notnull.NotNullAllArgs;
import com.paramichha.example.date.DateRecord;
import com.paramichha.example.decimal.DecimalRecord;
import com.paramichha.example.nested.NestedRecord;
import com.paramichha.example.number.NumberRecord;
import com.paramichha.example.realworld.payment.Payment;
import com.paramichha.example.realworld.registration.UserRegistration;
import com.paramichha.example.realworld.trading.Trade;
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
class TestKitRunnerTest {

    // ── single class generation ───────────────────────────────────────────

    @Nested
    @DisplayName("TestKit.of() — single class")
    class SingleClass {

        @Test
        void nullTypeThrows() {
            assertThatThrownBy(() -> TestKit.of(null))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void blankPackageThrows() {
            assertThatThrownBy(() -> TestKit.of(UserRegistration.class).withPackage(""))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void generatesRecord() {
            String src = TestKit.of(UserRegistration.class).generate().asString();
            assertStructure(src, "UserRegistration");
            assertHasValidator(src);
        }

        @Test
        void generatesAllArgs() {
            String src = TestKit.of(NotNullAllArgs.class).generate().asString();
            assertStructure(src, "NotNullAllArgs");
            assertHasValidator(src);
        }

        @Test
        void generatesTemporal() {
            String src = TestKit.of(DateRecord.class).generate().asString();
            assertStructure(src, "DateRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesNested() {
            String src = TestKit.of(NestedRecord.class).generate().asString();
            assertStructure(src, "NestedRecord");
        }

        @Test
        void generatesUnannotated() {
            String src = TestKit.of(NumberRecord.class).generate().asString();
            assertStructure(src, "NumberRecord");
            assertNoValidator(src);
        }

        @Test
        void generatesSingleAnnotation() {
            String src = TestKit.of(NotNullRecord.class).generate().asString();
            assertStructure(src, "NotNullRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesCombinedAnnotation() {
            String src = TestKit.of(NumericConstraintRecord.class).generate().asString();
            assertStructure(src, "NumericConstraintRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesEmail() {
            String src = TestKit.of(EmailRecord.class).generate().asString();
            assertStructure(src, "EmailRecord");
            assertThat(src).contains("notanemail");
        }

        @Test
        void generatesCombined() {
            String src = TestKit.of(CombinedRecord.class).generate().asString();
            assertStructure(src, "CombinedRecord");
            assertHasValidator(src);
        }

        @Test
        void generatesPayment() {
            String src = TestKit.of(Payment.class).generate().asString();
            assertStructure(src, "Payment");
            assertThat(src).contains("new Payment(");
        }

        @Test
        void generatesTrade() {
            String src = TestKit.of(Trade.class).generate().asString();
            assertStructure(src, "Trade");
        }

        @Test
        void defaultPackageMatchesClass() {
            GeneratedTestClass result = TestKit.of(UserRegistration.class).generate();
            assertThat(result.packageName())
                    .isEqualTo(UserRegistration.class.getPackageName());
        }

        @Test
        void classNameHasTestSuffix() {
            GeneratedTestClass result = TestKit.of(UserRegistration.class).generate();
            assertThat(result.className()).isEqualTo("UserRegistrationTest");
        }

        @Test
        void bracesBalanced() {
            for (Class<?> c : coreFixtures()) {
                String src = TestKit.of(c).generate().asString();
                long opens  = src.chars().filter(ch -> ch == '{').count();
                long closes = src.chars().filter(ch -> ch == '}').count();
                assertThat(opens)
                        .as("unbalanced braces in %s", c.getSimpleName())
                        .isEqualTo(closes);
            }
        }

        @Test
        void writesFileToCorrectPath(@TempDir Path tmp) {
            Path written = TestKit.of(UserRegistration.class)
                    .withPackage("com.example.out")
                    .generate()
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
            List<GeneratedTestClass> results = TestKitRunner
                    .scanPackage("com.paramichha.example.realworld")
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