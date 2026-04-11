package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.annotation.AnnotationProxyFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests ProductionDefaults warning rules and AnnotationProxyFactory.
 *
 * ProductionDefaults no longer injects annotations — it logs warnings to stdout
 * when PRODUCTION mode detects missing constraints on a field.
 *
 * Structure:
 *   Warnings   → each warning rule fires (or doesn't) based on declared annotations
 *   Silence    → no warnings for types that should never warn (Boolean, Enum, Optional)
 *   ProxyFactory → AnnotationProxyFactory create methods
 */
@DisplayName("ProductionDefaults")
class ProductionDefaultsTest {

    private static List<Annotation> anns(Annotation... annotations) {
        return new ArrayList<>(List.of(annotations));
    }

    /** Captures stdout during warn() call and returns it. */
    private static String captureWarn(String name, Class<?> type, List<Annotation> declared) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(buf));
        try {
            ProductionDefaults.warn(name, type, declared);
        } finally {
            System.setOut(old);
        }
        return buf.toString();
    }

    // =========================================================================
    // Warnings — rules that fire
    // =========================================================================

    @Nested
    @DisplayName("Warnings — rules that fire")
    class WarningRules {

        @Test
        @DisplayName("String without @NotNull → warns about null bypassing constraints")
        void string_missingNotNull() {
            String out = captureWarn("email", String.class,
                    anns(AnnotationProxyFactory.create(Email.class)));
            assertThat(out).contains("NotNull");
        }

        @Test
        @DisplayName("Integer without @Min/@Max → warns about missing bounds")
        void integer_missingBounds() {
            String out = captureWarn("age", Integer.class, anns());
            assertThat(out).contains("Min");
        }

        @Test
        @DisplayName("int primitive without @Min/@Max → warns about missing bounds")
        void intPrimitive_missingBounds() {
            String out = captureWarn("count", int.class, anns());
            assertThat(out).contains("Min");
        }

        @Test
        @DisplayName("List without @NotEmpty/@Size → warns about empty collection")
        void list_missingNotEmpty() {
            String out = captureWarn("items", List.class, anns());
            assertThat(out).contains("NotEmpty");
        }

        @Test
        @DisplayName("LocalDate without temporal constraint → warns any date accepted")
        void localDate_missingTemporal() {
            String out = captureWarn("dob", java.time.LocalDate.class, anns());
            assertThat(out).contains("temporal");
        }

        @Test
        @DisplayName("@Valid without @NotNull → warns null skips cascade")
        void valid_missingNotNull() {
            String out = captureWarn("address",
                    com.paramichha.datafactory.fixture.AnnotatedWrappersRequest.class,
                    anns(AnnotationProxyFactory.create(Valid.class)));
            assertThat(out).contains("NotNull");
        }

        @Test
        @DisplayName("@Size without @NotNull → warns null bypasses size")
        void size_missingNotNull() {
            String out = captureWarn("name", String.class,
                    anns(AnnotationProxyFactory.create(Size.class)));
            assertThat(out).contains("NotNull");
        }
    }

    // =========================================================================
    // Warnings — rules that do NOT fire
    // =========================================================================

    @Nested
    @DisplayName("Warnings — silence rules")
    class SilenceRules {

        @Test
        @DisplayName("@NotNull declared → no null warning")
        void notNull_declared_noWarn() {
            String out = captureWarn("email", String.class,
                    anns(AnnotationProxyFactory.create(NotNull.class),
                         AnnotationProxyFactory.create(Email.class)));
            assertThat(out).doesNotContain("null bypasses");
        }

        @Test
        @DisplayName("@Min declared → no bounds warning")
        void min_declared_noWarn() {
            String out = captureWarn("age", Integer.class,
                    anns(AnnotationProxyFactory.createMin(0)));
            assertThat(out).doesNotContain("Min/Max");
        }

        @Test
        @DisplayName("Boolean → no warnings ever")
        void boolean_noWarn() {
            String out = captureWarn("active", Boolean.class, anns());
            assertThat(out).isEmpty();
        }

        @Test
        @DisplayName("Enum → no warnings ever")
        void enum_noWarn() {
            String out = captureWarn("status", TestEnum.class, anns());
            assertThat(out).isEmpty();
        }

        @Test
        @DisplayName("Optional → no warnings ever")
        void optional_noWarn() {
            String out = captureWarn("opt", Optional.class, anns());
            assertThat(out).isEmpty();
        }

        @Test
        @DisplayName("LocalDate with @Future → no temporal warning")
        void localDate_withFuture_noWarn() {
            String out = captureWarn("expiresAt", java.time.LocalDate.class,
                    anns(AnnotationProxyFactory.create(Future.class)));
            assertThat(out).doesNotContain("temporal");
        }

        enum TestEnum { A, B }
    }

    // =========================================================================
    // AnnotationProxyFactory
    // =========================================================================

    @Nested
    @DisplayName("AnnotationProxyFactory")
    class AnnotationProxyBehaviour {

        @Test
        @DisplayName("createMin() returns @Min with correct value")
        void shouldCreateMinWithValue() {
            Min ann = AnnotationProxyFactory.createMin(42L);
            assertThat(ann.annotationType()).isEqualTo(Min.class);
            assertThat(ann.value()).isEqualTo(42L);
        }

        @Test
        @DisplayName("createMax() returns @Max with correct value")
        void shouldCreateMaxWithValue() {
            Max ann = AnnotationProxyFactory.createMax(100L);
            assertThat(ann.annotationType()).isEqualTo(Max.class);
            assertThat(ann.value()).isEqualTo(100L);
        }

        @Test
        @DisplayName("createDecimalMin() returns @DecimalMin inclusive=true")
        void shouldCreateDecimalMinInclusive() {
            DecimalMin ann = AnnotationProxyFactory.createDecimalMin("0.01");
            assertThat(ann.value()).isEqualTo("0.01");
            assertThat(ann.inclusive()).isTrue();
        }

        @Test
        @DisplayName("createDecimalMax() returns @DecimalMax inclusive=true")
        void shouldCreateDecimalMaxInclusive() {
            DecimalMax ann = AnnotationProxyFactory.createDecimalMax("999.99");
            assertThat(ann.value()).isEqualTo("999.99");
            assertThat(ann.inclusive()).isTrue();
        }

        @Test
        @DisplayName("create() toString includes annotation type name")
        void shouldHaveReadableToString() {
            var ann = AnnotationProxyFactory.create(NotNull.class);
            assertThat(ann.toString()).contains("NotNull");
        }

        @Test
        @DisplayName("create() equals only same proxy instance")
        void shouldEqualOnlySelf() {
            var ann = AnnotationProxyFactory.create(NotNull.class);
            assertThat(ann.equals(ann)).isTrue();
            assertThat(ann.equals(AnnotationProxyFactory.create(NotNull.class))).isFalse();
        }
    }
}
