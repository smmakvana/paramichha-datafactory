package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for value generation. Delegates to {@link TypeShaperRegistry}.
 * Never changes — add a new type by implementing {@link DataCreator}.
 */
public final class DataGenerator {
    private static final TypeShaperRegistry REGISTRY = TypeShaperRegistry.INSTANCE;
    private DataGenerator() {}

    public static Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        if ("null".equals(target.label())) return null;
        return REGISTRY.shape(field, target, machine);
    }

    /**
     * Produces all values for a field — one per boundary target.
     * Enum creators return a {@code List} of constants expanded inline.
     */
    public static List<Object> shapeAll(FieldConstraints field, List<BoundaryTarget> targets, DataMachine machine) {
        List<Object> results = new ArrayList<>();
        for (BoundaryTarget target : targets) {
            Object value = shape(field, target, machine);
            if (value instanceof List<?> list) {
                results.addAll(list);
            } else {
                results.add(value);
            }
        }
        results.removeIf(java.util.Objects::isNull);
        return List.copyOf(results);
    }
}
