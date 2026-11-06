package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.annotation.AnnotationProxyFactory;
import com.paramichha.datafactory.annotation.AnnotationUtils;
import com.paramichha.datafactory.generation.BoundaryPlanner;
import com.paramichha.datafactory.generation.BoundaryTarget;
import com.paramichha.datafactory.utils.PrimitiveDefaults;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Rules that only unusual or malformed annotations reach. */
class ConstraintEdgeCasesTest {

    record Ranged(@Range(min = 1, max = 5) int level) {}
    record BadDecimals(@DecimalMin("abc") BigDecimal low, @DecimalMax("xyz") BigDecimal high) {}
    record TwoFormats(@Email @URL String contact) {}
    record Nested(String name) {}
    record Maps(@SuppressWarnings("rawtypes") Map raw, Map<String, List<String>> nestedValue) {}

    private static List<Annotation> annotationsOf(Class<?> record, String field) {
        return FieldDescriptor.extract(record).stream()
                .filter(f -> f.name().equals(field)).findFirst().orElseThrow().validations();
    }

    private static String stdout(Runnable r) {
        PrintStream original = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true));
        try { r.run(); } finally { System.setOut(original); }
        return buf.toString();
    }

    private static String stderr(Runnable r) {
        PrintStream original = System.err;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buf, true));
        try { r.run(); } finally { System.setErr(original); }
        return buf.toString();
    }

    @Test
    void rangeGivesIntegerBoundsFromAnnotationsAndFromText() {
        Annotation range = annotationsOf(Ranged.class, "level").getFirst();
        assertThat(ClassAnnotationReader.INSTANCE.classifyQuantity(range))
                .isEqualTo(new NumericBounds.IntegerBounds(1L, 5L));
        assertThat(TextAnnotationReader.INSTANCE.classifyQuantity("@Range(min=1, max=5)"))
                .isEqualTo(new NumericBounds.IntegerBounds(1L, 5L));
    }

    @Test
    void decimalBoundsThatAreNotNumbersAreIgnored() {
        assertThat(ClassAnnotationReader.INSTANCE.classifyQuantity(annotationsOf(BadDecimals.class, "low").getFirst()))
                .isNull();
        assertThat(ClassAnnotationReader.INSTANCE.classifyQuantity(annotationsOf(BadDecimals.class, "high").getFirst()))
                .isNull();
    }

    @Test
    void theFirstOfTwoFormatAnnotationsWinsAndTheConflictIsReported() {
        String err = stderr(() -> assertThat(AnnotationResolver.analyze(
                "contact", String.class, annotationsOf(TwoFormats.class, "contact")).format())
                .isEqualTo(FormatType.EMAIL));
        assertThat(err).contains("conflicting format annotations: EMAIL and URL");
    }

    @Test
    void validWithoutNotNullIsReported() {
        Valid valid = AnnotationProxyFactory.create(Valid.class);
        assertThat(stdout(() -> ConstraintChecker.warn("child", Nested.class, List.of(valid))))
                .contains("@Valid without @NotNull");
    }

    @Test
    void decimalBoundsConvenienceAccessorsTruncateAndUnboundedHasNone() {
        NumericBounds decimal = new NumericBounds.DecimalBounds(new BigDecimal("1.9"), new BigDecimal("8.2"));
        assertThat(decimal.min()).isEqualTo(1L);
        assertThat(decimal.max()).isEqualTo(8L);
        NumericBounds none = new NumericBounds.Unbounded();
        assertThat(none.min()).isNull();
        assertThat(none.max()).isNull();
        assertThat(none.isContradictory()).isFalse();
        assertThat(new NumericBounds.DecimalBounds(null, null).min()).isNull();
    }

    @Test
    void mapKeyAndValueTypesFallBackToStringWhenNotAPlainClass() {
        FieldDescriptor raw = FieldDescriptor.extract(Maps.class).get(0);
        FieldDescriptor nested = FieldDescriptor.extract(Maps.class).get(1);
        assertThat(raw.mapKeyType()).isEqualTo(String.class);
        assertThat(raw.mapValueType()).isEqualTo(String.class);
        assertThat(nested.mapKeyType()).isEqualTo(String.class);
        assertThat(nested.mapValueType()).isEqualTo(String.class); // List<String> is not a Class
    }

    @Test
    void equalDecimalBoundsPlanOneTargetAndDecimalBoundsOnACollectionAreIgnored() {
        BigDecimal five = new BigDecimal("5");
        FieldConstraints pinned = FieldConstraints.builder("rate", BigDecimal.class)
                .bounds(new NumericBounds.DecimalBounds(five, five)).build();
        assertThat(BoundaryPlanner.plan(pinned)).containsExactly(BoundaryTarget.atDecimalMin(five));

        FieldConstraints list = FieldConstraints.builder("items", List.class)
                .bounds(new NumericBounds.DecimalBounds(five, null)).build();
        assertThat(BoundaryPlanner.plan(list)).containsExactly(BoundaryTarget.semantic());
    }

    @Test
    void annotationHelpers() {
        assertThat(PrimitiveDefaults.of(String.class)).isNull();
        // "55" starts and ends with the same character but is not quoted
        assertThat(((Min) AnnotationUtils.parse("@Min(55)")).value()).isEqualTo(55L);
        Min min = AnnotationProxyFactory.createMin(3);
        // a proxy equals only itself (List.indexOf calls equals; AssertJ checks identity first)
        assertThat(List.of(min).indexOf(min)).isZero();
        assertThat(min).isNotEqualTo(AnnotationProxyFactory.createMin(3));
    }
}
