package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import net.datafaker.Faker;

/**
 * Produces {@code Boolean} values; returns {@code true} for the "true" target label.
 */
final class BooleanDataCreator implements DataCreator {

    static final BooleanDataCreator INSTANCE = new BooleanDataCreator();

    private BooleanDataCreator() {
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
    public Object shape(FieldConstraints field, BoundaryTarget target, Faker faker) {
        return "true".equals(target.label());
    }
}
