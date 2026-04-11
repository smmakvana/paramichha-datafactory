package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

import java.util.UUID;

/**
 * Produces random {@link UUID} values.
 */
final class UuidTypeShaper implements TypeShaper {

    static final UuidTypeShaper INSTANCE = new UuidTypeShaper();

    private UuidTypeShaper() {
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == UUID.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return UUID.randomUUID();
    }
}
