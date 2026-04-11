package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Produces numeric values for all Java numeric types.
 */
final class NumericTypeShaper implements TypeShaper {

    static final NumericTypeShaper INSTANCE = new NumericTypeShaper();

    private NumericTypeShaper() {
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Short.class || type == short.class
                || type == Byte.class || type == byte.class
                || type == Double.class || type == double.class
                || type == Float.class || type == float.class
                || type == BigDecimal.class
                || type == BigInteger.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        return NumericShaper.shape(field, target, faker);
    }
}
