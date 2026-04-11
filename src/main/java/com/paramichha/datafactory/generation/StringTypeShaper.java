package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/**
 * Produces {@code String} values; catch-all for text fields.
 */
final class StringTypeShaper implements TypeShaper {

    static final StringTypeShaper INSTANCE = new StringTypeShaper();

    private StringTypeShaper() {
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == String.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        return StringShaper.shape(field, target, faker);
    }
}
