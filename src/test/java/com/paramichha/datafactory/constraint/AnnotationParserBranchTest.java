package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.AnnotationParseException;
import com.paramichha.datafactory.builder.DefaultAnnotationParser;
import com.paramichha.datafactory.generation.BoundaryTarget;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Branch coverage for DefaultAnnotationParser, SingleArgumentHandler,
 * NoArgHandler, and BoundaryTarget.
 */
@DisplayName("Annotation parser and BoundaryTarget branch coverage")
class AnnotationParserBranchTest {

    // =========================================================================
    // DefaultAnnotationParser
    // =========================================================================

    @Nested
    @DisplayName("DefaultAnnotationParser")
    class DefaultAnnotationParserTests {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("INSTANCE is non-null singleton")
            void instanceNonNull() {
                assertThat(DefaultAnnotationParser.INSTANCE).isNotNull();
            }

            @Test @DisplayName("new instance parses same as INSTANCE")
            void newInstanceWorks() {
                var parser = new DefaultAnnotationParser();
                assertThat(parser.parse("@NotNull")).isNotNull();
            }
        }

        @Nested
        @DisplayName("Validation — parse() branches")
        class ParseBranches {

            @Test @DisplayName("no-arg annotation without parens")
            void noArgNoParens() {
                var ann = DefaultAnnotationParser.INSTANCE.parse("@NotNull");
                assertThat(ann.annotationType()).isEqualTo(NotNull.class);
            }

            @Test @DisplayName("no-arg annotation with empty parens")
            void noArgEmptyParens() {
                var ann = DefaultAnnotationParser.INSTANCE.parse("@NotNull()");
                assertThat(ann.annotationType()).isEqualTo(NotNull.class);
            }

            @Test @DisplayName("one-arg positional form @Min(18)")
            void oneArgPositional() {
                var ann = (Min) DefaultAnnotationParser.INSTANCE.parse("@Min(18)");
                assertThat(ann.value()).isEqualTo(18L);
            }

            @Test @DisplayName("one-arg named form @Min(value=18)")
            void oneArgNamed() {
                var ann = (Min) DefaultAnnotationParser.INSTANCE.parse("@Min(value=18)");
                assertThat(ann.value()).isEqualTo(18L);
            }

            @Test @DisplayName("one-arg string @Pattern(regexp=\"^[A-Z]+$\")")
            void oneArgString() {
                var ann = (Pattern) DefaultAnnotationParser.INSTANCE.parse(
                        "@Pattern(regexp=\"^[A-Z]+$\")");
                assertThat(ann.regexp()).isEqualTo("^[A-Z]+$");
            }

            @Test @DisplayName("one-arg named string @DecimalMin(value=\"0.01\")")
            void oneArgNamedString() {
                var ann = (DecimalMin) DefaultAnnotationParser.INSTANCE.parse(
                        "@DecimalMin(value=\"0.01\")");
                assertThat(ann.value()).isEqualTo("0.01");
            }

            @Test @DisplayName("multi-arg @Size(min=2, max=50)")
            void multiArg() {
                var ann = (Size) DefaultAnnotationParser.INSTANCE.parse("@Size(min=2, max=50)");
                assertThat(ann.min()).isEqualTo(2);
                assertThat(ann.max()).isEqualTo(50);
            }

            @Test @DisplayName("unknown annotation throws AnnotationParseException")
            void unknownThrows() {
                assertThatThrownBy(() ->
                    DefaultAnnotationParser.INSTANCE.parse("@UnknownAnnotation")
                ).isInstanceOf(AnnotationParseException.class);
            }

            @Test @DisplayName("missing @ prefix throws AnnotationParseException")
            void missingAtThrows() {
                assertThatThrownBy(() ->
                    DefaultAnnotationParser.INSTANCE.parse("NotNull")
                ).isInstanceOf(AnnotationParseException.class);
            }

            @Test @DisplayName("malformed value throws AnnotationParseException")
            void malformedValueThrows() {
                assertThatThrownBy(() ->
                    DefaultAnnotationParser.INSTANCE.parse("@Min(notanumber)")
                ).isInstanceOf(AnnotationParseException.class);
            }

            @Test @DisplayName("getSupportedAnnotation returns null for unknown")
            void getSupportedAnnotationUnknown() {
                assertThat(DefaultAnnotationParser.INSTANCE.getSupportedAnnotation("Unknown"))
                        .isNull();
            }

            @Test @DisplayName("getAnnotationType returns null for unknown class")
            void getAnnotationTypeUnknown() {
                assertThat(DefaultAnnotationParser.INSTANCE.getAnnotationType(Override.class))
                        .isNull();
            }
        }
    }

    // =========================================================================
    // SingleArgumentHandler — coerce() and stripQuotes() branches
    // =========================================================================

    @Nested
    @DisplayName("SingleArgumentHandler — coerce and stripQuotes")
    class SingleArgumentHandlerTests {

        @Nested
        @DisplayName("Validation — value coercion")
        class Coercion {

            @Test @DisplayName("long value coerced for @Min")
            void longCoerced() {
                var ann = (Min) DefaultAnnotationParser.INSTANCE.parse("@Min(100)");
                assertThat(ann.value()).isEqualTo(100L);
            }

            @Test @DisplayName("String value kept for @Pattern")
            void stringKept() {
                var ann = (Pattern) DefaultAnnotationParser.INSTANCE.parse(
                        "@Pattern(regexp=\"[A-Z]+\")");
                assertThat(ann.regexp()).isEqualTo("[A-Z]+");
            }

            @Test @DisplayName("single-quoted string stripped")
            void singleQuoteStripped() {
                var ann = (DecimalMax) DefaultAnnotationParser.INSTANCE.parse(
                        "@DecimalMax(value='9.99')");
                assertThat(ann.value()).isEqualTo("9.99");
            }

            @Test @DisplayName("unmatched quotes not stripped")
            void unmatchedQuotesKept() {
                assertThatThrownBy(() ->
                    DefaultAnnotationParser.INSTANCE.parse("@Min(\"abc\")")
                ).isInstanceOf(AnnotationParseException.class);
            }

            @Test @DisplayName("named form with spaces: value = 42")
            void namedWithSpaces() {
                var ann = (Max) DefaultAnnotationParser.INSTANCE.parse("@Max(value = 42)");
                assertThat(ann.value()).isEqualTo(42L);
            }

            @Test @DisplayName("empty raw string throws")
            void emptyRawThrows() {
                assertThatThrownBy(() ->
                    DefaultAnnotationParser.INSTANCE.parse("@Min()")
                ).isInstanceOf(AnnotationParseException.class);
            }
        }
    }

    // =========================================================================
    // NoArgHandler — throws when attributes given
    // =========================================================================

    @Nested
    @DisplayName("NoArgHandler — rejects attributes")
    class NoArgHandlerTests {

        @Nested
        @DisplayName("Validation")
        class Validation {
            @Test @DisplayName("no attributes parses correctly")
            void noAttributesOk() {
                assertThat(DefaultAnnotationParser.INSTANCE.parse("@NotNull")).isNotNull();
            }

            @Test @DisplayName("attributes given throws AnnotationParseException")
            void attributesThrows() {
                assertThatThrownBy(() ->
                    DefaultAnnotationParser.INSTANCE.parse("@NotNull(someValue)")
                ).isInstanceOf(AnnotationParseException.class);
            }
        }
    }

    // =========================================================================
    // BoundaryTarget — label() and targetQuantity() for all variants
    // =========================================================================

    @Nested
    @DisplayName("BoundaryTarget")
    class BoundaryTargetTests {

        @Nested
        @DisplayName("Construction")
        class Construction {
            @Test @DisplayName("semantic() creates Semantic with label")
            void semantic() {
                assertThat(BoundaryTarget.semantic().label()).isEqualTo("semantic");
            }
            @Test @DisplayName("atMin() creates Fixed with label atMin")
            void atMin() {
                assertThat(BoundaryTarget.atMin(1L).label()).isEqualTo("atMin");
            }
            @Test @DisplayName("atMax() creates Fixed with label atMax")
            void atMax() {
                assertThat(BoundaryTarget.atMax(10L).label()).isEqualTo("atMax");
            }
            @Test @DisplayName("atDecimalMin() creates DecimalFixed with label atMin")
            void atDecimalMin() {
                assertThat(BoundaryTarget.atDecimalMin(BigDecimal.ONE).label()).isEqualTo("atMin");
            }
            @Test @DisplayName("atDecimalMax() creates DecimalFixed with label atMax")
            void atDecimalMax() {
                assertThat(BoundaryTarget.atDecimalMax(BigDecimal.TEN).label()).isEqualTo("atMax");
            }
            @Test @DisplayName("nullTarget() creates Special with label null")
            void nullTarget() {
                assertThat(BoundaryTarget.nullTarget().label()).isEqualTo("null");
            }
            @Test @DisplayName("trueTarget() creates Special with label true")
            void trueTarget() {
                assertThat(BoundaryTarget.trueTarget().label()).isEqualTo("true");
            }
            @Test @DisplayName("falseTarget() creates Special with label false")
            void falseTarget() {
                assertThat(BoundaryTarget.falseTarget().label()).isEqualTo("false");
            }
            @Test @DisplayName("allEnumValues() creates Special")
            void allEnumValues() {
                assertThat(BoundaryTarget.allEnumValues().label()).isEqualTo("allEnumValues");
            }
        }

        @Nested
        @DisplayName("Validation — targetQuantity()")
        class TargetQuantity {
            @Test @DisplayName("Fixed returns value as Long")
            void fixedReturnsValue() {
                assertThat(BoundaryTarget.atMin(42L).targetQuantity()).isEqualTo(42L);
            }
            @Test @DisplayName("DecimalFixed returns longValue()")
            void decimalFixedReturnsLong() {
                assertThat(BoundaryTarget.atDecimalMin(BigDecimal.TEN).targetQuantity())
                        .isEqualTo(10L);
            }
            @Test @DisplayName("Semantic returns null")
            void semanticReturnsNull() {
                assertThat(BoundaryTarget.semantic().targetQuantity()).isNull();
            }
            @Test @DisplayName("Special returns null")
            void specialReturnsNull() {
                assertThat(BoundaryTarget.nullTarget().targetQuantity()).isNull();
            }
        }

        @Nested
        @DisplayName("Behaviour — isSemantic() and isFixed()")
        class TypeChecks {
            @Test @DisplayName("semantic() isSemantic() true")
            void semanticIsSemantic() {
                assertThat(BoundaryTarget.semantic().isSemantic()).isTrue();
            }
            @Test @DisplayName("Fixed isFixed() true")
            void fixedIsFixed() {
                assertThat(BoundaryTarget.atMin(1L).isFixed()).isTrue();
            }
            @Test @DisplayName("DecimalFixed isFixed() true")
            void decimalIsFixed() {
                assertThat(BoundaryTarget.atDecimalMin(BigDecimal.ONE).isFixed()).isTrue();
            }
            @Test @DisplayName("Special isSemantic() false")
            void specialNotSemantic() {
                assertThat(BoundaryTarget.nullTarget().isSemantic()).isFalse();
            }
            @Test @DisplayName("Special isFixed() false")
            void specialNotFixed() {
                assertThat(BoundaryTarget.nullTarget().isFixed()).isFalse();
            }
            @Test @DisplayName("justAboveMin label")
            void justAboveMin() {
                assertThat(BoundaryTarget.justAboveMin(1L).label()).isEqualTo("justAboveMin");
            }
            @Test @DisplayName("midpoint label")
            void midpoint() {
                assertThat(BoundaryTarget.midpoint(1L, 10L).label()).isEqualTo("midpoint");
            }
            @Test @DisplayName("justBelowMax label")
            void justBelowMax() {
                assertThat(BoundaryTarget.justBelowMax(10L).label()).isEqualTo("justBelowMax");
            }
            @Test @DisplayName("typeDefaultNegative label")
            void typeDefaultNeg() {
                assertThat(BoundaryTarget.typeDefaultNegative().label()).isEqualTo("negative");
            }
            @Test @DisplayName("typeDefaultZero label")
            void typeDefaultZero() {
                assertThat(BoundaryTarget.typeDefaultZero().label()).isEqualTo("zero");
            }
            @Test @DisplayName("justAboveDecimalMin label")
            void justAboveDecimalMin() {
                assertThat(BoundaryTarget.justAboveDecimalMin(BigDecimal.ONE).label())
                        .isEqualTo("justAboveMin");
            }
            @Test @DisplayName("decimalMidpoint label")
            void decimalMidpoint() {
                assertThat(BoundaryTarget.decimalMidpoint(BigDecimal.ONE, BigDecimal.TEN).label())
                        .isEqualTo("midpoint");
            }
            @Test @DisplayName("justBelowDecimalMax label")
            void justBelowDecimalMax() {
                assertThat(BoundaryTarget.justBelowDecimalMax(BigDecimal.TEN).label())
                        .isEqualTo("justBelowMax");
            }
        }
    }
}
