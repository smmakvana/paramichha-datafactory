package com.paramichha.datafactory.analyzer;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnnotationAnalyzer")
class AnnotationAnalyzerTest {

    // ── helpers ────────────────────────────────────────────────────────────

    private FieldConstraints analyze(String name, Class<?> type, Annotation... annotations) {
        return AnnotationAnalyzer.analyze(name, type, List.of(annotations));
    }

    private FieldConstraints str(String name, Annotation... annotations) {
        return analyze(name, String.class, annotations);
    }

    private FieldConstraints integer(String name, Annotation... annotations) {
        return analyze(name, Integer.class, annotations);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. NO ANNOTATIONS
    // ═══════════════════════════════════════════════════════════════════════

    private NotNull annNotNull() {
        return proxy(NotNull.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. PRESENCE
    // ═══════════════════════════════════════════════════════════════════════

    private Null annNull() {
        return proxy(Null.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. FORMAT
    // ═══════════════════════════════════════════════════════════════════════

    private NotBlank annNotBlank() {
        return proxy(NotBlank.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. QUANTITY — strings
    // ═══════════════════════════════════════════════════════════════════════

    private NotEmpty annNotEmpty() {
        return proxy(NotEmpty.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. QUANTITY — numbers
    // ═══════════════════════════════════════════════════════════════════════

    private Email annEmail() {
        return proxy(Email.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. TEMPORAL
    // ═══════════════════════════════════════════════════════════════════════

    private URL annUrl() {
        return proxy(URL.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7. BOOLEAN
    // ═══════════════════════════════════════════════════════════════════════

    private Past annPast() {
        return proxy(Past.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 8. COMBINATIONS
    // ═══════════════════════════════════════════════════════════════════════

    private PastOrPresent annPastOrPresent() {
        return proxy(PastOrPresent.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 9. CONFLICTS
    // ═══════════════════════════════════════════════════════════════════════

    private Future annFuture() {
        return proxy(Future.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 10. CODEGEN MODE
    // ═══════════════════════════════════════════════════════════════════════

    private FutureOrPresent annFutureOrPresent() {
        return proxy(FutureOrPresent.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ANNOTATION FACTORIES
    // ═══════════════════════════════════════════════════════════════════════

    private AssertTrue annAssertTrue() {
        return proxy(AssertTrue.class);
    }

    private AssertFalse annAssertFalse() {
        return proxy(AssertFalse.class);
    }

    private Positive annPositive() {
        return proxy(Positive.class);
    }

    private PositiveOrZero annPositiveOrZero() {
        return proxy(PositiveOrZero.class);
    }

    private Negative annNegative() {
        return proxy(Negative.class);
    }

    private NegativeOrZero annNegativeOrZero() {
        return proxy(NegativeOrZero.class);
    }

    private Min annMin(long value) {
        return new Min() {
            public long value() {
                return value;
            }

            public String message() {
                return "";
            }

            public Class<?>[] groups() {
                return new Class[0];
            }

            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            public Class<? extends Annotation> annotationType() {
                return Min.class;
            }
        };
    }

    private Max annMax(long value) {
        return new Max() {
            public long value() {
                return value;
            }

            public String message() {
                return "";
            }

            public Class<?>[] groups() {
                return new Class[0];
            }

            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            public Class<? extends Annotation> annotationType() {
                return Max.class;
            }
        };
    }

    private Size annSize(int sizeMin, int sizeMax) {
        return new Size() {
            public int min() {
                return sizeMin;
            }

            public int max() {
                return sizeMax;
            }

            public String message() {
                return "";
            }

            public Class<?>[] groups() {
                return new Class[0];
            }

            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            public Class<? extends Annotation> annotationType() {
                return Size.class;
            }
        };
    }

    private Length annLength(int lenMin, int lenMax) {
        return new Length() {
            public int min() {
                return lenMin;
            }

            public int max() {
                return lenMax;
            }

            public String message() {
                return "";
            }

            public Class<?>[] groups() {
                return new Class[0];
            }

            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }
        };
    }

    private Pattern annPattern(String regexp) {
        return new Pattern() {
            public String regexp() {
                return regexp;
            }

            public Pattern.Flag[] flags() {
                return new Pattern.Flag[0];
            }

            public String message() {
                return "";
            }

            public Class<?>[] groups() {
                return new Class[0];
            }

            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            public Class<? extends Annotation> annotationType() {
                return Pattern.class;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> A proxy(Class<A> type) {
        return (A) java.lang.reflect.Proxy.newProxyInstance(
                type.getClassLoader(), new Class[]{type},
                (proxy, method, args) -> switch (method.getName()) {
                    case "annotationType" -> type;
                    case "groups" -> new Class[0];
                    case "payload" -> new Class[0];
                    case "message" -> "";
                    default -> method.getDefaultValue();
                });
    }

    @Nested
    @DisplayName("No annotations")
    class NoAnnotations {

        @Test
        void string_isNullable_noFormat_noBounds() {
            var f = str("name");
            assertThat(f.nullable()).isTrue();
            assertThat(f.format()).isEqualTo(FormatType.NONE);
            assertThat(f.bounds()).isEqualTo(QuantityBounds.unbounded());
            assertThat(f.warnings()).isEmpty();
        }

        @Test
        void integer_isNullable_noBounds() {
            var f = integer("count");
            assertThat(f.nullable()).isTrue();
            assertThat(f.bounds()).isEqualTo(QuantityBounds.unbounded());
        }

        @Test
        void temporal_noDirection() {
            var f = analyze("createdAt", Instant.class);
            assertThat(f.temporal()).isEqualTo(TemporalDirection.NONE);
        }
    }

    @Nested
    @DisplayName("Presence annotations")
    class Presence {

        @Test
        void notNull_nonNullable() {
            assertThat(str("x", annNotNull()).nullable()).isFalse();
        }

        @Test
        void notBlank_nonNullable() {
            assertThat(str("x", annNotBlank()).nullable()).isFalse();
        }

        @Test
        void notEmpty_nonNullable() {
            assertThat(str("x", annNotEmpty()).nullable()).isFalse();
        }

        @Test
        void nullAnnotation_mustBeNull() {
            var f = str("deprecated", annNull());
            assertThat(f.mustBeNull()).isTrue();
        }
    }

    @Nested
    @DisplayName("Format annotations")
    class Format {

        @Test
        void email() {
            assertThat(str("email", annEmail()).format()).isEqualTo(FormatType.EMAIL);
        }

        @Test
        void url() {
            assertThat(str("site", annUrl()).format()).isEqualTo(FormatType.URL);
        }

        @Test
        void none() {
            assertThat(str("name", annNotBlank()).format()).isEqualTo(FormatType.NONE);
        }

        @Test
        void pattern_storesRegexp() {
            var f = str("code", annPattern("^[A-Z]{2}\\d{4}$"));
            assertThat(f.format()).isEqualTo(FormatType.PATTERN);
            assertThat(f.patternRegexp()).isEqualTo("^[A-Z]{2}\\d{4}$");
        }
    }

    @Nested
    @DisplayName("@Size and @Length")
    class SizeLength {

        @Test
        void size_bothBounds() {
            var f = str("name", annSize(2, 50));
            assertThat(f.bounds().min()).isEqualTo(2L);
            assertThat(f.bounds().max()).isEqualTo(50L);
        }

        @Test
        void size_minOnly() {
            var f = str("name", annSize(2, Integer.MAX_VALUE));
            assertThat(f.bounds().min()).isEqualTo(2L);
            assertThat(f.bounds().max()).isNull();
        }

        @Test
        void size_maxOnly() {
            var f = str("name", annSize(0, 50));
            assertThat(f.bounds().min()).isNull();
            assertThat(f.bounds().max()).isEqualTo(50L);
        }

        @Test
        void length_sameBehaviourAsSize() {
            var f = str("name", annLength(8, 50));
            assertThat(f.bounds().min()).isEqualTo(8L);
            assertThat(f.bounds().max()).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("Numeric bounds")
    class NumericBounds {

        @Test
        void min() {
            assertThat(integer("age", annMin(18)).bounds().min()).isEqualTo(18L);
        }

        @Test
        void max() {
            assertThat(integer("age", annMax(120)).bounds().max()).isEqualTo(120L);
        }

        @Test
        void positive() {
            assertThat(integer("n", annPositive()).bounds().min()).isEqualTo(1L);
        }

        @Test
        void positiveOrZero() {
            assertThat(integer("n", annPositiveOrZero()).bounds().min()).isEqualTo(0L);
        }

        @Test
        void negative() {
            assertThat(integer("n", annNegative()).bounds().max()).isEqualTo(-1L);
        }

        @Test
        void negativeOrZero() {
            assertThat(integer("n", annNegativeOrZero()).bounds().max()).isEqualTo(0L);
        }

        @Test
        void positive_and_min_takesHigher() {
            // @Positive → min=1, @Min(18) → take 18
            assertThat(integer("age", annPositive(), annMin(18)).bounds().min()).isEqualTo(18L);
        }

        @Test
        void positive_subsumes_minZero() {
            // @Positive → min=1, @Min(0) → @Positive wins (1 > 0)
            assertThat(integer("n", annPositive(), annMin(0)).bounds().min()).isEqualTo(1L);
        }

        @Test
        void min_and_max_bothSet() {
            var f = integer("age", annMin(18), annMax(120));
            assertThat(f.bounds().min()).isEqualTo(18L);
            assertThat(f.bounds().max()).isEqualTo(120L);
        }
    }

    @Nested
    @DisplayName("Temporal annotations")
    class Temporal {

        @Test
        void past() {
            assertThat(analyze("d", LocalDate.class, annPast()).temporal()).isEqualTo(TemporalDirection.PAST);
        }

        @Test
        void pastOrPresent() {
            assertThat(analyze("d", LocalDate.class, annPastOrPresent()).temporal()).isEqualTo(TemporalDirection.PAST_OR_NOW);
        }

        @Test
        void future() {
            assertThat(analyze("d", Instant.class, annFuture()).temporal()).isEqualTo(TemporalDirection.FUTURE);
        }

        @Test
        void futureOrPresent() {
            assertThat(analyze("d", Instant.class, annFutureOrPresent()).temporal()).isEqualTo(TemporalDirection.FUTURE_OR_NOW);
        }

        @Test
        void none() {
            assertThat(analyze("d", LocalDateTime.class, annNotNull()).temporal()).isEqualTo(TemporalDirection.NONE);
        }
    }

    @Nested
    @DisplayName("Boolean annotations")
    class BooleanAnnotations {

        @Test
        void assertTrueAnnotation() {
            var f = analyze("active", Boolean.class, annAssertTrue());
            assertThat(f.assertTrueRequired()).isTrue();
            assertThat(f.assertFalseRequired()).isFalse();
        }

        @Test
        void assertFalseAnnotation() {
            var f = analyze("deleted", Boolean.class, annAssertFalse());
            assertThat(f.assertFalseRequired()).isTrue();
            assertThat(f.assertTrueRequired()).isFalse();
        }
    }

    @Nested
    @DisplayName("Annotation combinations")
    class Combinations {

        @Test
        void notBlank_email_size() {
            var f = str("email", annNotBlank(), annEmail(), annSize(8, 50));
            assertThat(f.nullable()).isFalse();
            assertThat(f.format()).isEqualTo(FormatType.EMAIL);
            assertThat(f.bounds().min()).isEqualTo(8L);
            assertThat(f.bounds().max()).isEqualTo(50L);
            assertThat(f.warnings()).isEmpty();
        }

        @Test
        void notBlank_size_noFormat() {
            var f = str("firstName", annNotBlank(), annSize(2, 50));
            assertThat(f.format()).isEqualTo(FormatType.NONE);
            assertThat(f.bounds().min()).isEqualTo(2L);
            assertThat(f.bounds().max()).isEqualTo(50L);
        }

        @Test
        void positive_and_max() {
            var f = integer("rating", annPositive(), annMax(5));
            assertThat(f.bounds().min()).isEqualTo(1L);
            assertThat(f.bounds().max()).isEqualTo(5L);
        }

        @Test
        void notNull_past() {
            var f = analyze("dob", LocalDate.class, annNotNull(), annPast());
            assertThat(f.nullable()).isFalse();
            assertThat(f.temporal()).isEqualTo(TemporalDirection.PAST);
        }
    }

    @Nested
    @DisplayName("Conflicting annotations produce warnings")
    class Conflicts {

        @Test
        void past_and_future_warns() {
            var f = analyze("d", LocalDate.class, annPast(), annFuture());
            assertThat(f.warnings()).anyMatch(w -> w.contains("Past") && w.contains("Future"));
        }

        @Test
        void assertTrue_and_assertFalse_warns() {
            var f = analyze("flag", Boolean.class, annAssertTrue(), annAssertFalse());
            assertThat(f.warnings()).anyMatch(w -> w.contains("AssertTrue") && w.contains("AssertFalse"));
        }

        @Test
        void minGreaterThanMax_warns() {
            var f = integer("n", annMin(100), annMax(50));
            assertThat(f.warnings()).anyMatch(w -> w.contains("Min") || w.contains("Max"));
        }
    }

    @Nested
    @DisplayName("Codegen mode — annotation strings")
    class CodegenMode {

        @Test
        void notBlank_email() {
            var f = AnnotationAnalyzer.analyzeFromStrings("email", "String",
                    List.of("@NotBlank", "@Email"));
            assertThat(f.nullable()).isFalse();
            assertThat(f.format()).isEqualTo(FormatType.EMAIL);
        }

        @Test
        void min_max() {
            var f = AnnotationAnalyzer.analyzeFromStrings("age", "Integer",
                    List.of("@Min(18)", "@Max(120)"));
            assertThat(f.bounds().min()).isEqualTo(18L);
            assertThat(f.bounds().max()).isEqualTo(120L);
        }

        @Test
        void size_parsesMinMax() {
            var f = AnnotationAnalyzer.analyzeFromStrings("name", "String",
                    List.of("@Size(min=2, max=50)"));
            assertThat(f.bounds().min()).isEqualTo(2L);
            assertThat(f.bounds().max()).isEqualTo(50L);
        }

        @Test
        void pattern_extractsRegexp() {
            var f = AnnotationAnalyzer.analyzeFromStrings("code", "String",
                    List.of("@Pattern(regexp=\"^[A-Z]{2}\\\\d{4}$\")"));
            assertThat(f.format()).isEqualTo(FormatType.PATTERN);
            assertThat(f.patternRegexp()).isEqualTo("^[A-Z]{2}\\d{4}$");
        }

        @Test
        void positive_setsMinOne() {
            var f = AnnotationAnalyzer.analyzeFromStrings("count", "Integer",
                    List.of("@Positive"));
            assertThat(f.bounds().min()).isEqualTo(1L);
        }

        @Test
        void past_setsPastDirection() {
            var f = AnnotationAnalyzer.analyzeFromStrings("dob", "LocalDate",
                    List.of("@Past"));
            assertThat(f.temporal()).isEqualTo(TemporalDirection.PAST);
        }
    }
}
