package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/** Produces {@code Boolean} values — {@code true} for the "true" target label. */
final class BooleanDataCreator implements DataCreator {
    static final BooleanDataCreator INSTANCE = new BooleanDataCreator();

    private BooleanDataCreator() {}

    @Override
    public boolean supports(Class<?> type) {
        return type == Boolean.class || type == boolean.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        return "true".equals(target.label());
    }
}
