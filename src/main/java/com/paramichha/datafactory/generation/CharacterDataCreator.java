package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/** Produces {@code Character} / {@code char} values. */
final class CharacterDataCreator implements DataCreator {
    static final CharacterDataCreator INSTANCE = new CharacterDataCreator();
    private CharacterDataCreator() {}

    @Override public boolean supports(Class<?> type) {
        return type == Character.class || type == char.class;
    }

    @Override public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        return 'A';
    }
}
