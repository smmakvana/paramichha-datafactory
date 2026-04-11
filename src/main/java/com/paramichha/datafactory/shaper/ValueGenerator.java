package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for value generation. Delegates to {@link TypeShaperRegistry}.
 * This class never changes — add a new type by implementing {@link TypeShaper}.
 */
public final class ValueGenerator {

    private static final TypeShaperRegistry REGISTRY = TypeShaperRegistry.INSTANCE;

    private ValueGenerator() {
    }

    /**
     * Produces one value for the field at the given boundary target.
     */
    public static Object shape(FieldConstraints field, BoundaryTarget target) {
        if ("null".equals(target.label())) return null;
        return REGISTRY.shape(field, target);
    }

    /**
     * Produces all valid values for a field — one per boundary target.
     * Enum shapers return a {@code List} of constants which is expanded inline.
     * Unrecognised types produce {@code null} from the registry; those are removed
     * before returning since {@link List#copyOf} rejects nulls.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> shapeAll(FieldConstraints field, List<BoundaryTarget> targets) {
        List<Object> results = new ArrayList<>();
        for (BoundaryTarget target : targets) {
            Object value = shape(field, target);
            if (value instanceof List<?> list) {
                results.addAll((List<Object>) list);
            } else {
                results.add(value);
            }
        }
        results.removeIf(java.util.Objects::isNull);
        return List.copyOf(results);
    }
}
