package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

import java.util.List;

/**
 * Returns all enum constants as a {@code List}.
 * {@link ValueGenerator#shapeAll} expands the list into individual values.
 */
final class EnumTypeShaper implements TypeShaper {

    static final EnumTypeShaper INSTANCE = new EnumTypeShaper();

    private EnumTypeShaper() {
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
