package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/**
 * Produces {@code Boolean} values; returns {@code true} for the "true" target label.
 */
final class BooleanTypeShaper implements TypeShaper {

    static final BooleanTypeShaper INSTANCE = new BooleanTypeShaper();

    private BooleanTypeShaper() {
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Boolean.class || type == boolean.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        return "true".equals(target.label());
    }
}
