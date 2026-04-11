package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

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
        return 'A';
    }
}
