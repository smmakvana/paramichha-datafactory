package com.paramichha.datafactory.analyzer;

/**
 * Classifies a single annotation token into constraint categories.
 *
 * <p>Two implementations:
 * <ul>
 *   <li>{@link RuntimeAnnotationClassifier} — real {@code Annotation} instances from reflection
 *   <li>{@link StringAnnotationClassifier} — annotation strings from YAML, e.g. {@code "@Min(18)"}
 * </ul>
 *
 * <p>{@link AnnotationAnalyzer} runs the same five-pass pipeline against either implementation.
 */
interface AnnotationClassifier {

    boolean isNotNull(Object annotation);

    boolean isNotBlank(Object annotation);

    boolean isNotEmpty(Object annotation);

    boolean isNull(Object annotation);

    boolean isValid(Object annotation);

    FormatType classifyFormat(Object annotation);

    /**
     * Returns the {@code regexp} attribute if the annotation is {@code @Pattern}, otherwise {@code null}.
     */
    String extractPatternRegexp(Object annotation);

    /**
     * Returns bounds if the annotation constrains quantity, otherwise {@code null}.
     */
    QuantityBounds classifyQuantity(Object annotation);

    /**
     * Returns the integer-digit count if the annotation is {@code @Digits}, otherwise {@code -1}.
     */
    int extractIntegerDigits(Object annotation);

    /**
     * Returns the fraction-digit count if the annotation is {@code @Digits}, otherwise {@code -1}.
     */
    int extractFractionDigits(Object annotation);

    TemporalDirection classifyTemporal(Object annotation);

    boolean isAssertTrue(Object annotation);

    boolean isAssertFalse(Object annotation);
}
