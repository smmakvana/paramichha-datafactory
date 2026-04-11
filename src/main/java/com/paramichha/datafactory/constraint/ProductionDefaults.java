package com.paramichha.datafactory.constraint;

import jakarta.validation.constraints.*;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Analyses a field's declared annotations and logs warnings when the contract
 * is incomplete for production use.
 *
 * <p>Warnings are surfaced when {@link com.paramichha.datafactory.GenerationMode#PRODUCTION}
 * is active. No annotations are injected — DataFactory only works with what the
 * developer declared.
 *
 * <h2>Warning rules</h2>
 * <ul>
 *   <li>Reference field with no {@code @NotNull/@NotBlank/@NotEmpty} → null bypasses all other constraints</li>
 *   <li>Numeric field with no {@code @Min/@Max} → only type boundary enforced, no business rule</li>
 *   <li>Collection with no {@code @NotEmpty/@Size} → empty list may be unintended</li>
 *   <li>Temporal field with no temporal constraint → any date accepted</li>
 *   <li>{@code @Valid} without {@code @NotNull} → null skips cascade validation</li>
 *   <li>{@code @Size/@Length} without {@code @NotNull} → null bypasses size check</li>
 * </ul>
 */
public final class ProductionDefaults {

    private ProductionDefaults() {}

    public static void warn(String fieldName, Class<?> fieldType, List<Annotation> declared) {
        if (neverWarn(fieldType)) return;

        boolean hasNullConstraint = hasAny(declared, NotNull.class, NotBlank.class, NotEmpty.class);
        boolean hasNumericBound   = hasAny(declared, Min.class, Max.class,
                Positive.class, PositiveOrZero.class, Negative.class, NegativeOrZero.class);
        boolean hasSizeConstraint = hasAny(declared, Size.class)
                || hasAnyByName(declared, "Length");
        boolean hasTemporalConstraint = hasAny(declared, Past.class, PastOrPresent.class,
                Future.class, FutureOrPresent.class);
        boolean hasCascadeValid = hasAny(declared, jakarta.validation.Valid.class);
        boolean isCollection    = isCollection(fieldType);
        boolean isNumeric       = isNumeric(fieldType);
        boolean isTemporal      = isTemporal(fieldType);

        if (!fieldType.isPrimitive() && !hasNullConstraint) {
            log(fieldName, "missing @NotNull — null bypasses all other constraints at runtime. "
                    + "Declare @NotNull if null should be invalid.");
        }
        if (isNumeric && !hasNumericBound) {
            log(fieldName, "missing @Min/@Max — only type boundary enforced ("
                    + typeBoundSummary(fieldType) + "). Declare bounds to express your business rule.");
        }
        if (isCollection && !hasAny(declared, NotEmpty.class) && !hasSizeConstraint) {
            log(fieldName, "missing @NotEmpty or @Size — empty collection may be unintended.");
        }
        if (isTemporal && !hasTemporalConstraint) {
            log(fieldName, "missing temporal constraint (@Past/@Future etc) — any date accepted.");
        }
        if (hasCascadeValid && !hasNullConstraint && !fieldType.isPrimitive()) {
            log(fieldName, "@Valid without @NotNull — null skips cascade validation entirely.");
        }
        if (hasSizeConstraint && !hasNullConstraint) {
            log(fieldName, "@Size/@Length without @NotNull — null bypasses size validation by Jakarta spec.");
        }
    }

    private static void log(String fieldName, String message) {
        System.out.println("⚠  DataFactory [PRODUCTION] '" + fieldName + "': " + message);
    }

    private static String typeBoundSummary(Class<?> t) {
        if (t == Byte.class    || t == byte.class)  return "Byte.MIN to Byte.MAX";
        if (t == Short.class   || t == short.class) return "Short.MIN to Short.MAX";
        if (t == Integer.class || t == int.class)   return "Integer.MIN to Integer.MAX";
        if (t == Long.class    || t == long.class)  return "Long.MIN to Long.MAX";
        return "unbounded";
    }

    private static boolean neverWarn(Class<?> type) {
        return type == Boolean.class || type == boolean.class
            || type.isEnum()
            || type == java.util.Optional.class;
    }

    private static boolean isCollection(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    static boolean isNumeric(Class<?> t) {
        return t == Integer.class   || t == int.class
            || t == Long.class      || t == long.class
            || t == Short.class     || t == short.class
            || t == Byte.class      || t == byte.class
            || t == Float.class     || t == float.class
            || t == Double.class    || t == double.class
            || t == java.math.BigDecimal.class
            || t == java.math.BigInteger.class;
    }

    private static boolean isTemporal(Class<?> t) {
        return t == java.time.LocalDate.class
            || t == java.time.LocalDateTime.class
            || t == java.time.ZonedDateTime.class
            || t == java.time.OffsetDateTime.class
            || t == java.time.Instant.class
            || t == java.util.Date.class
            || t == java.time.Year.class
            || t == java.time.YearMonth.class;
    }

    @SafeVarargs
    static boolean hasAny(List<Annotation> annotations, Class<? extends Annotation>... types) {
        for (Annotation a : annotations)
            for (Class<? extends Annotation> t : types)
                if (t.isInstance(a)) return true;
        return false;
    }

    static boolean hasAnyByName(List<Annotation> annotations, String... names) {
        for (Annotation a : annotations)
            for (String name : names)
                if (a.annotationType().getSimpleName().equals(name)) return true;
        return false;
    }
}
