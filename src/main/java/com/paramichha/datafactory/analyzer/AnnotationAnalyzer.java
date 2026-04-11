package com.paramichha.datafactory.analyzer;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Reads all validation annotations on a field and produces a {@link FieldConstraints}.
 *
 * <p>Runs a single five-pass pipeline (presence → format → quantity → temporal → boolean)
 * against the supplied {@link AnnotationClassifier}. The classifier handles the difference
 * between real {@code Annotation} instances and annotation strings from YAML.
 */
public final class AnnotationAnalyzer {

    private AnnotationAnalyzer() {
    }

    /**
     * Analyzes a field at runtime using real {@code Annotation} instances.
     */
    public static FieldConstraints analyze(String fieldName, Class<?> fieldType,
                                           List<Annotation> annotations) {
        return runPipeline(fieldName, fieldType, annotations, RuntimeAnnotationClassifier.INSTANCE);
    }

    /**
     * Analyzes a field at codegen time using annotation strings from YAML.
     */
    public static FieldConstraints analyzeFromStrings(String fieldName, String fieldTypeName,
                                                      List<String> annotationStrings) {
        return runPipeline(fieldName, resolveType(fieldTypeName), annotationStrings,
                StringAnnotationClassifier.INSTANCE);
    }

    private static <A> FieldConstraints runPipeline(String fieldName, Class<?> fieldType,
                                                    List<A> annotations,
                                                    AnnotationClassifier classifier) {
        FieldConstraints.Builder b = FieldConstraints.builder(fieldName, fieldType);

        if (isIntegerType(fieldType)) b.integerOnly(true);

        // Pass 1 — presence
        for (A a : annotations) {
            if (classifier.isNotNull(a) || classifier.isNotBlank(a) || classifier.isNotEmpty(a)) {
                b.nullable(false);
            }
            if (classifier.isNull(a)) b.mustBeNull(true);
            if (classifier.isValid(a)) b.cascadeValid(true);
        }

        // Pass 2 — format
        FormatType fmt = FormatType.NONE;
        int fmtCount = 0;
        for (A a : annotations) {
            FormatType f = classifier.classifyFormat(a);
            if (f != FormatType.NONE) {
                fmtCount++;
                if (fmt == FormatType.NONE) {
                    fmt = f;
                    String re = classifier.extractPatternRegexp(a);
                    if (re != null) b.patternRegexp(re);
                }
            }
        }
        if (fmtCount > 1) b.warn("Multiple format annotations on '" + fieldName + "' — using '" + fmt + "'.");
        b.format(fmt);

        // Pass 3 — quantity
        QuantityBounds bounds = QuantityBounds.unbounded();
        for (A a : annotations) {
            QuantityBounds ab = classifier.classifyQuantity(a);
            if (ab != null) bounds = bounds.intersect(ab);
            int intDigits = classifier.extractIntegerDigits(a);
            int fracDigits = classifier.extractFractionDigits(a);
            if (intDigits >= 0) b.maxIntegerDigits(intDigits);
            if (fracDigits >= 0) b.maxFractionDigits(fracDigits);
        }
        if (bounds.isContradictory()) {
            b.warn("Min(" + bounds.min() + ") > Max(" + bounds.max() + ") on '" + fieldName + "'.");
        }
        b.bounds(bounds);

        // Pass 4 — temporal
        TemporalDirection temporal = TemporalDirection.NONE;
        boolean hasPast = false, hasFuture = false;
        for (A a : annotations) {
            TemporalDirection t = classifier.classifyTemporal(a);
            if (t != TemporalDirection.NONE) {
                if (temporal == TemporalDirection.NONE) temporal = t;
                if (t == TemporalDirection.PAST || t == TemporalDirection.PAST_OR_NOW) hasPast = true;
                if (t == TemporalDirection.FUTURE || t == TemporalDirection.FUTURE_OR_NOW) hasFuture = true;
            }
        }
        if (hasPast && hasFuture) {
            b.warn("Both Past and Future on '" + fieldName + "' — contradictory. Using Past.");
        }
        b.temporal(temporal);

        // Pass 5 — boolean
        boolean hasTrue = annotations.stream().anyMatch(classifier::isAssertTrue);
        boolean hasFalse = annotations.stream().anyMatch(classifier::isAssertFalse);
        if (hasTrue && hasFalse) {
            b.warn("Both @AssertTrue and @AssertFalse on '" + fieldName + "' — contradictory.");
        }
        b.assertTrueRequired(hasTrue);
        b.assertFalseRequired(hasFalse);

        return b.build();
    }

    private static boolean isIntegerType(Class<?> t) {
        return t == Integer.class || t == int.class
                || t == Long.class || t == long.class
                || t == Short.class || t == short.class
                || t == Byte.class || t == byte.class;
    }

    static Class<?> resolveType(String name) {
        return switch (name) {
            case "Integer", "int" -> Integer.class;
            case "Long", "long" -> Long.class;
            case "Double", "double" -> Double.class;
            case "Float", "float" -> Float.class;
            case "Boolean", "boolean" -> Boolean.class;
            case "Character", "char" -> Character.class;
            case "Short", "short" -> Short.class;
            case "Byte", "byte" -> Byte.class;
            case "BigDecimal" -> java.math.BigDecimal.class;
            case "BigInteger" -> java.math.BigInteger.class;
            case "Instant" -> java.time.Instant.class;
            case "LocalDate" -> java.time.LocalDate.class;
            case "LocalDateTime" -> java.time.LocalDateTime.class;
            case "LocalTime" -> java.time.LocalTime.class;
            case "ZonedDateTime" -> java.time.ZonedDateTime.class;
            case "OffsetDateTime" -> java.time.OffsetDateTime.class;
            case "UUID" -> java.util.UUID.class;
            case "Date" -> java.util.Date.class;
            case "Year" -> java.time.Year.class;
            case "YearMonth" -> java.time.YearMonth.class;
            case "List" -> java.util.List.class;
            case "Collection" -> java.util.Collection.class;
            default -> String.class;
        };
    }
}
