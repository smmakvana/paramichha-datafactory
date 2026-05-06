package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import java.util.UUID;

/** Produces UUID values — deterministic when machine is seeded, random otherwise. */
final class UuidDataCreator implements DataCreator {
    static final UuidDataCreator INSTANCE = new UuidDataCreator();
    private UuidDataCreator() {}

    @Override public boolean supports(Class<?> type) { return type == UUID.class; }

    @Override public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        return UUID.fromString(machine.text("uuid"));
    }
}
