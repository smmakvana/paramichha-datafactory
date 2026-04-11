package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Produces realistic numbers shaped to hit a {@link BoundaryTarget}, returning the correct Java type.
 */
final class NumericShaper {

    private static final Faker FAKER = new Faker(Locale.ENGLISH);

    private NumericShaper() {
    }

    static Object shape(FieldConstraints field, BoundaryTarget target) {
        Class<?> type = field.fieldType();
        long value = resolveValue(field, target);

        if (type == Integer.class || type == int.class) return (int) clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (type == Long.class || type == long.class) return value;
        if (type == Short.class || type == short.class) return (short) clamp(value, Short.MIN_VALUE, Short.MAX_VALUE);
        if (type == Byte.class || type == byte.class) return (byte) clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
        if (type == Double.class || type == double.class) return shapeDouble(field, target);
        if (type == Float.class || type == float.class) return (float) shapeDouble(field, target);
        if (type == BigDecimal.class) return shapeBigDecimal(field, target);
        if (type == BigInteger.class) return BigInteger.valueOf(value);
        return value;
    }

    private static long resolveValue(FieldConstraints field, BoundaryTarget target) {
        if (target.targetQuantity() != null) return target.targetQuantity();
        return semanticValue(field);
    }

    private static long semanticValue(FieldConstraints field) {
        Long min = field.bounds().min();
        Long max = field.bounds().max();
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
        return FAKER.number().numberBetween(1L, 1000L);
    }

    private static long bounded(long preferred, Long min, Long max) {
        long result = preferred;
        if (min != null && result < min) result = min;
        if (max != null && result > max) result = max;
        return result;
    }

    private static double shapeDouble(FieldConstraints field, BoundaryTarget target) {
        if (target.targetQuantity() != null) return (double) target.targetQuantity();
        Long min = field.bounds().min();
        Long max = field.bounds().max();
        if (min != null && max != null) return (double) (min + max) / 2.0;
        if (min != null) return min + 0.5;
        if (max != null) return max - 0.5;
        return FAKER.number().randomDouble(2, 1, 1000);
    }

    private static BigDecimal shapeBigDecimal(FieldConstraints field, BoundaryTarget target) {
        int scale = field.maxFractionDigits() == Integer.MAX_VALUE ? 2 : field.maxFractionDigits();
        if (target.targetQuantity() != null) {
            return BigDecimal.valueOf(target.targetQuantity()).setScale(scale, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(shapeDouble(field, target)).setScale(scale, RoundingMode.HALF_UP);
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}
