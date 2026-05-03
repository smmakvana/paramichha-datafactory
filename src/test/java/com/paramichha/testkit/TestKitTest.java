package com.paramichha.testkit;

import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullMixed;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationFullString;
import com.paramichha.testkit.fixture.annotation.combined.AnnotationNotNullMinMax;
import com.paramichha.testkit.fixture.realworld.registration.RealWorldRegistration;
import com.paramichha.testkit.fixture.strategy.lombok.StrategyLombokBuilderAnnotated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TestKit")
class TestKitTest {

    @Nested @DisplayName("of()")
    class OfMethod {

        @Test
        void null_type_throws() {
            assertThatThrownBy(() -> TestKit.of(null))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void returns_testkit_instance() {
            assertThat(TestKit.of(RealWorldRegistration.class)).isNotNull();
        }
    }

    @Nested @DisplayName("generate()")
    class GenerateMethod {

        @Test
        void generates_source_for_record() {
            String src = TestKit.of(RealWorldRegistration.class)
                    .withPackage("com.example.test").generate().asString();
            assertThat(src).contains("package com.example.test");
            assertThat(src).contains("class RealWorldRegistrationTest");
            assertThat(src).contains("private final RealWorldRegistration validInstance");
            assertThat(src).contains("class SmokeTest");
        }

        @Test
        void generates_source_for_lombok_builder() {
            String src = TestKit.of(StrategyLombokBuilderAnnotated.class)
                    .withPackage("com.example.test").generate().asString();
            assertThat(src).contains("StrategyLombokBuilderAnnotated.builder()");
            assertThat(src).contains(".build()");
        }

        @Test
        void default_package_is_class_package() {
            GeneratedTestClass result = TestKit.of(RealWorldRegistration.class).generate();
            assertThat(result.packageName())
                    .isEqualTo(RealWorldRegistration.class.getPackageName());
        }

        @Test
        void class_name_is_type_name_plus_suffix() {
            GeneratedTestClass result = TestKit.of(RealWorldRegistration.class).generate();
            assertThat(result.className()).isEqualTo("RealWorldRegistrationTest");
        }

        @Test
        void source_contains_field_nested_classes() {
            String src = TestKit.of(AnnotationNotNullMinMax.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).contains("@Nested");
        }

        @Test
        void source_contains_valid_boundary_values() {
            String src = TestKit.of(AnnotationNotNullMinMax.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).containsAnyOf("18", "19", "20");
            assertThat(src).containsAnyOf("120", "119");
        }

        @Test
        void source_contains_invalid_values() {
            String src = TestKit.of(AnnotationFullString.class)
                    .withPackage("com.example").generate().asString();
            assertThat(src).contains("null");
            assertThat(src).contains("notanemail");
        }

        @Test
        void source_compiles_as_valid_java_structure() {
            String src = TestKit.of(AnnotationFullMixed.class)
                    .withPackage("com.example").generate().asString();
            long opens  = src.chars().filter(c -> c == '{').count();
            long closes = src.chars().filter(c -> c == '}').count();
            assertThat(opens).isEqualTo(closes);
        }
    }

    @Nested @DisplayName("withPackage()")
    class WithPackage {

        @Test
        void blank_package_throws() {
            assertThatThrownBy(() -> TestKit.of(RealWorldRegistration.class).withPackage(""))
                    .isInstanceOf(TestKitException.class);
        }

        @Test
        void package_appears_in_output() {
            String src = TestKit.of(RealWorldRegistration.class)
                    .withPackage("com.custom.pkg").generate().asString();
            assertThat(src).contains("package com.custom.pkg;");
        }
    }

    @Nested @DisplayName("GeneratedTestClass")
    class GeneratedTestClassTests {

        @Test
        void asString_returns_source() {
            GeneratedTestClass result = TestKit.of(RealWorldRegistration.class).generate();
            assertThat(result.asString()).isEqualTo(result.source());
        }

        @Test
        void writeTo_creates_file(@TempDir Path tmp) {
            GeneratedTestClass result = TestKit.of(RealWorldRegistration.class)
                    .withPackage("com.example.out").generate();
            Path written = result.writeTo(tmp);
            assertThat(written).exists();
            assertThat(written.getFileName().toString())
                    .isEqualTo("RealWorldRegistrationTest.java");
        }
    }
}
