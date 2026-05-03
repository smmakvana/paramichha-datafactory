package com.paramichha.testkit;

import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullMixed;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullString;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullMinMax;
import com.paramichha.testkit.fixture.annotation.notnull.AnnotationNotNullString;
import com.paramichha.testkit.fixture.mixed.MixedAllAnnotated;
import com.paramichha.testkit.fixture.nested.onelevel.NestedOneLevel;
import com.paramichha.testkit.fixture.primitive.intp.PrimitiveInt;
import com.paramichha.testkit.fixture.primitive.intp.PrimitiveIntMany;
import com.paramichha.testkit.fixture.realworld.payment.RealWorldPayment;
import com.paramichha.testkit.fixture.realworld.registration.RealWorldRegistration;
import com.paramichha.testkit.fixture.realworld.trading.RealWorldTrade;
import com.paramichha.testkit.fixture.strategy.lombok.StrategyLombokBuilderAnnotated;
import com.paramichha.testkit.fixture.strategy.record.StrategyRecordAnnotated;
import com.paramichha.testkit.fixture.temporal.localdate.TemporalLocalDate;
import com.paramichha.testkit.fixture.wrapper.bigdecimalw.WrapperBigDecimal;
import com.paramichha.testkit.fixture.wrapper.integerw.WrapperInteger;
import com.paramichha.testkit.runner.TestKitRunner;
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
        void null_type_throws() {
            assertThatThrownBy(() -> TestKit.of(null))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void blank_package_throws() {
            assertThatThrownBy(() -> TestKit.of(RealWorldRegistration.class).withPackage(""))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void generates_record() {
            String src = TestKit.of(RealWorldRegistration.class).generate().asString();
            assertStructure(src, "RealWorldRegistration");
            assertHasValidator(src); // RealWorldRegistration has Jakarta annotations
            assertThat(src).contains("\"notanemail\"");
        }

        @Test
        void generates_lombok_builder() {
            String src = TestKit.of(StrategyLombokBuilderAnnotated.class).generate().asString();
            assertStructure(src, "StrategyLombokBuilderAnnotated");
            assertThat(src).contains("StrategyLombokBuilderAnnotated.builder()");
            assertThat(src).contains(".build()");
        }

        @Test
        void generates_temporal() {
            String src = TestKit.of(TemporalLocalDate.class).generate().asString();
            assertStructure(src, "TemporalLocalDate");
            assertNoValidator(src); // TemporalLocalDate has no Jakarta annotations
        }

        @Test
        void generates_nested() {
            String src = TestKit.of(NestedOneLevel.class).generate().asString();
            assertStructure(src, "NestedOneLevel");
        }

        @Test
        void generates_primitive() {
            String src = TestKit.of(PrimitiveInt.class).generate().asString();
            assertStructure(src, "PrimitiveInt");
            assertNoValidator(src); // PrimitiveInt has no Jakarta annotations
        }

        @Test
        void generates_primitive_many() {
            String src = TestKit.of(PrimitiveIntMany.class).generate().asString();
            assertStructure(src, "PrimitiveIntMany");
            assertNoValidator(src); // PrimitiveIntMany has no Jakarta annotations
        }

        @Test
        void generates_wrapper() {
            String src = TestKit.of(WrapperInteger.class).generate().asString();
            assertStructure(src, "WrapperInteger");
            assertNoValidator(src); // WrapperInteger has no Jakarta annotations
        }

        @Test
        void generates_single_annotation() {
            String src = TestKit.of(AnnotationNotNullString.class).generate().asString();
            assertStructure(src, "AnnotationNotNullString");
        }

        @Test
        void generates_combined_annotation() {
            String src = TestKit.of(AnnotationNotNullMinMax.class).generate().asString();
            assertStructure(src, "AnnotationNotNullMinMax");
        }

        @Test
        void generates_full_string_annotation() {
            String src = TestKit.of(AnnotationFullString.class).generate().asString();
            assertStructure(src, "AnnotationFullString");
            assertThat(src).contains("\"notanemail\"");
        }

        @Test
        void generates_mixed_all_annotated() {
            String src = TestKit.of(MixedAllAnnotated.class).generate().asString();
            assertStructure(src, "MixedAllAnnotated");
        }

        @Test
        void generates_payment() {
            String src = TestKit.of(RealWorldPayment.class).generate().asString();
            assertStructure(src, "RealWorldPayment");
            assertThat(src).contains("new RealWorldPayment(");
        }

        @Test
        void generates_trade() {
            String src = TestKit.of(RealWorldTrade.class).generate().asString();
            assertStructure(src, "RealWorldTrade");
        }

        @Test
        void default_package_matches_class() {
            GeneratedTestClass result = TestKit.of(RealWorldRegistration.class).generate();
            assertThat(result.packageName())
                    .isEqualTo(RealWorldRegistration.class.getPackageName());
        }

        @Test
        void class_name_has_validation_test_suffix() {
            GeneratedTestClass result = TestKit.of(RealWorldRegistration.class).generate();
            assertThat(result.className()).isEqualTo("RealWorldRegistrationTest");
        }

        @Test
        void braces_balanced() {
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
        void writes_file_to_correct_path(@TempDir Path tmp) {
            Path written = TestKit.of(RealWorldRegistration.class)
                    .withPackage("com.example.out")
                    .generate()
                    .writeTo(tmp);
            assertThat(written).exists();
            assertThat(written.getFileName().toString())
                    .isEqualTo("RealWorldRegistrationTest.java");
            assertThat(written.getParent().toString()).endsWith("out");
        }
    }

    // ── batch runner ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("TestKitRunner")
    class Runner {

        @Test
        void forClasses_generates_all(@TempDir Path tmp) {
            List<Path> written = TestKitRunner
                    .forClasses(RealWorldRegistration.class, RealWorldPayment.class,
                            StrategyLombokBuilderAnnotated.class)
                    .writeTo(tmp);
            assertThat(written).hasSize(3);
            written.forEach(p -> assertThat(p).exists());
        }

        @Test
        void forClasses_each_file_ends_with_suffix(@TempDir Path tmp) {
            TestKitRunner.forClasses(coreFixtures().toArray(Class[]::new))
                    .writeTo(tmp)
                    .forEach(p -> assertThat(p.getFileName().toString())
                            .endsWith("Test.java"));
        }

        @Test
        void targetCount_matches_input() {
            assertThat(TestKitRunner
                    .forClasses(coreFixtures().toArray(Class[]::new))
                    .targetCount())
                    .isEqualTo(coreFixtures().size());
        }

        @Test
        void scanPackage_finds_testable_classes() {
            List<GeneratedTestClass> results = TestKitRunner
                    .scanPackage("com.paramichha.testkit.fixture.realworld")
                    .generate();
            assertThat(results).isNotEmpty();
            results.forEach(g ->
                    assertThat(g.className()).endsWith("Test"));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────

    /**
     * Asserts the structural skeleton every generated test must have,
     * regardless of whether the class has Jakarta validation annotations.
     */
    private static void assertStructure(String src, String className) {
        assertThat(src).contains("class " + className + "Test");
        assertThat(src).contains("private final " + className + " validInstance");
        assertThat(src).contains("class Construction");
        assertThat(src).contains("class Validation");
        assertThat(src).contains("validInstanceIsCreated");
        assertThat(src).contains("validInstanceFieldsAreCorrect");
        assertThat(src).contains("validInstancePassesValidation");
        assertThat(src).contains("List.of(");
    }

    /**
     * Asserts that the generated test includes the Jakarta validator setup.
     * Only call this for classes that actually have Jakarta validation annotations.
     */
    private static void assertHasValidator(String src) {
        assertThat(src).contains("private static Validator validator");
        assertThat(src).contains("Validation.buildDefaultValidatorFactory()");
    }

    /**
     * Asserts that no validator setup is generated for plain POJOs
     * (classes with no Jakarta validation annotations).
     */
    private static void assertNoValidator(String src) {
        assertThat(src).doesNotContain("private static Validator validator");
        assertThat(src).doesNotContain("jakarta.validation.Validation");
    }

    private static List<Class<?>> coreFixtures() {
        return List.of(
                PrimitiveInt.class,
                PrimitiveIntMany.class,
                WrapperInteger.class,
                WrapperBigDecimal.class,
                TemporalLocalDate.class,
                AnnotationNotNullString.class,
                AnnotationNotNullMinMax.class,
                AnnotationFullMixed.class,
                MixedAllAnnotated.class,
                StrategyRecordAnnotated.class,
                StrategyLombokBuilderAnnotated.class,
                RealWorldRegistration.class,
                RealWorldPayment.class,
                RealWorldTrade.class
        );
    }
}