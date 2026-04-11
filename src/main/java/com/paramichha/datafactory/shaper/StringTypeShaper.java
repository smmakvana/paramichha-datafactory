package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

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
        return StringShaper.shape(field, target);
    }
}
