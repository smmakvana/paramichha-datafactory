package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.QuantityBounds;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Produces realistic numbers shaped to hit a {@link BoundaryTarget}, returning the correct Java type.
 */
public final class NumericShaper {

    // Faker provided externally — see shape(field, target, faker)

    private NumericShaper() {
    }

    /** Convenience overload — uses a random Faker. Used by tests and internal callers. */
    static Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    static Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        Class<?> type = field.fieldType();
        long value = resolveValue(field, target, faker);

        if (type == Integer.class || type == int.class) return (int) clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (type == Long.class || type == long.class) return value;
        if (type == Short.class || type == short.class) return (short) clamp(value, Short.MIN_VALUE, Short.MAX_VALUE);
        if (type == Byte.class || type == byte.class) return (byte) clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
        if (type == Double.class || type == double.class) return shapeDouble(field, target, faker);
        if (type == Float.class || type == float.class) return (float) shapeDouble(field, target, faker);
        if (type == BigDecimal.class) return shapeBigDecimal(field, target, faker);
        if (type == BigInteger.class) return BigInteger.valueOf(value);
        return value;
    }

    private static long resolveValue(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        if (target instanceof BoundaryTarget.Fixed f) return f.value();
        if (target instanceof BoundaryTarget.DecimalFixed d) return d.value().longValue();
        return semanticValue(field, faker);
    }

    private static long semanticValue(FieldConstraints field, net.datafaker.Faker faker) {
        Long min = intMin(field.bounds());
        Long max = intMax(field.bounds());
        String name = field.fieldName().toLowerCase();

        if (name.contains("age")) return bounded(30L, min, max);
        if (name.contains("year")) return bounded(2024L, min, max);
        if (name.contains("count") || name.contains("total")) return bounded(1L, min, max);
        if (name.contains("page")) return bounded(0L, min, max);
        if (name.contains("retry") || name.contains("attempt")) return bounded(0L, min, max);
        if (name.contains("rating") || name.contains("score")) return bounded(5L, min, max);
        if (name.contains("price") || name.contains("amount")) return bounded(100L, min, max);
        if (name.contains("quantity") || name.contains("qty")) return bounded(1L, min, max);

        if (min != null && max != null) return (min + max) / 2;
        if (min != null) return min + 1;
        if (max != null) return max - 1;
        return faker.number().numberBetween(1L, 1000L);
    }

    private static long bounded(long preferred, Long min, Long max) {
        long result = preferred;
        if (min != null && result < min) result = min;
        if (max != null && result > max) result = max;
        return result;
    }

    private static double shapeDouble(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        if (target instanceof BoundaryTarget.DecimalFixed d) return d.value().doubleValue();
        if (target instanceof BoundaryTarget.Fixed f) return (double) f.value();
        Long min = intMin(field.bounds());
        Long max = intMax(field.bounds());
        if (min != null && max != null) return (double) (min + max) / 2.0;
        if (min != null) return min + 0.5;
        if (max != null) return max - 0.5;
        return faker.number().randomDouble(2, 1, 1000);
    }

    private static BigDecimal shapeBigDecimal(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        if (target instanceof BoundaryTarget.DecimalFixed d) return d.value().setScale(resolveScale(field), java.math.RoundingMode.HALF_UP);
        int scale = field.maxFractionDigits() == Integer.MAX_VALUE ? 2 : field.maxFractionDigits();
        if (target instanceof BoundaryTarget.Fixed f) {
            return BigDecimal.valueOf(f.value()).setScale(scale, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(shapeDouble(field, target, faker)).setScale(scale, RoundingMode.HALF_UP);
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int resolveScale(FieldConstraints field) {
        int s = field.maxFractionDigits();
        return (s > 0 && s != Integer.MAX_VALUE) ? s : 2;
    }

    private static Long intMin(QuantityBounds b) {
        return switch (b) {
            case QuantityBounds.IntegerBounds i -> i.min();
            case QuantityBounds.DecimalBounds d -> d.decMin() != null ? d.decMin().longValue() : null;
            case QuantityBounds.Unbounded u     -> null;
        };
    }

    private static Long intMax(QuantityBounds b) {
        return switch (b) {
            case QuantityBounds.IntegerBounds i -> i.max();
            case QuantityBounds.DecimalBounds d -> d.decMax() != null ? d.decMax().longValue() : null;
            case QuantityBounds.Unbounded u     -> null;
        };
    }
}
