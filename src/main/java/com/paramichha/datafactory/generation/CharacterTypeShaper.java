package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/**
 * Produces {@code Character} / {@code char} values.
 */
final class CharacterTypeShaper implements TypeShaper {

    static final CharacterTypeShaper INSTANCE = new CharacterTypeShaper();

    private CharacterTypeShaper() {
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Character.class || type == char.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        return 'A';
    }
}
