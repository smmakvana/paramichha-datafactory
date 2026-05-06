package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/** Produces {@code String} values. */
final class StringDataCreator implements DataCreator {
    static final StringDataCreator INSTANCE = new StringDataCreator();
    private StringDataCreator() {}

    @Override public boolean supports(Class<?> type) { return type == String.class; }

    @Override public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        return StringShaper.shape(field, target, machine);
    }
}
