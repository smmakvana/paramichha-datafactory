package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

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
        return "true".equals(target.label());
    }
}
