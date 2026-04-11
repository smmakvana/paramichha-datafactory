package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

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
        return List.of(field.fieldType().getEnumConstants());
    }
}
