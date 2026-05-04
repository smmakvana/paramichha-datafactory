package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

import java.util.List;

/**
 * Returns all enum constants as a {@code List}.
 * {@link DataGenerator#shapeAll} expands the list into individual values.
 */
final class EnumDataCreator implements DataCreator {

    static final EnumDataCreator INSTANCE = new EnumDataCreator();

    private EnumDataCreator() {
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.isEnum();
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        return List.of(field.fieldType().getEnumConstants());
    }
}
