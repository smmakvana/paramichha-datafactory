package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

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
        return shape(field, target, FakerProvider.random());
    }

    public static Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        if ("null".equals(target.label())) return null;
        return REGISTRY.shape(field, target, faker);
    }

    public static List<Object> shapeAll(FieldConstraints field, List<BoundaryTarget> targets) {
        return shapeAll(field, targets, FakerProvider.random());
    }

    /**
     * Produces all valid values for a field — one per boundary target.
     * Enum shapers return a {@code List} of constants which is expanded inline.
     * Unrecognised types produce {@code null} from the registry; those are removed
     * before returning since {@link List#copyOf} rejects nulls.
     */
    public static List<Object> shapeAll(FieldConstraints field, List<BoundaryTarget> targets, net.datafaker.Faker faker) {
        List<Object> results = new ArrayList<>();
        for (BoundaryTarget target : targets) {
            Object value = shape(field, target, faker);
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
