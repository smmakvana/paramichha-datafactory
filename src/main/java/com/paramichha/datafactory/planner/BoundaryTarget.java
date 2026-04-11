package com.paramichha.datafactory.planner;

import com.paramichha.datafactory.shaper.ValueGenerator;

/**
 * A single target point for {@link ValueGenerator} to hit.
 *
 * @param label          human-readable name used in test method naming, e.g. "atMin", "semantic"
 * @param targetQuantity exact length (strings) or value (numbers) to produce;
 *                       {@code null} means use the semantic default
 */
public record BoundaryTarget(String label, Long targetQuantity) {

    public static BoundaryTarget semantic() {
        return new BoundaryTarget("semantic", null);
    }

    public static BoundaryTarget atMin(long min) {
        return new BoundaryTarget("atMin", min);
    }

    public static BoundaryTarget justAboveMin(long min) {
        return new BoundaryTarget("justAboveMin", min + 1);
    }

    public static BoundaryTarget midpoint(long mn, long mx) {
        return new BoundaryTarget("midpoint", (mn + mx) / 2);
    }

    public static BoundaryTarget justBelowMax(long max) {
        return new BoundaryTarget("justBelowMax", max - 1);
    }

    public static BoundaryTarget atMax(long max) {
        return new BoundaryTarget("atMax", max);
    }

    public static BoundaryTarget nullTarget() {
        return new BoundaryTarget("null", null);
    }

    public static BoundaryTarget trueTarget() {
        return new BoundaryTarget("true", 1L);
    }

    public static BoundaryTarget falseTarget() {
        return new BoundaryTarget("false", 0L);
    }

    public static BoundaryTarget allEnumValues() {
        return new BoundaryTarget("allEnumValues", null);
    }

    public boolean isSemantic() {
        return targetQuantity == null && "semantic".equals(label);
    }

    public boolean isFixed() {
        return targetQuantity != null;
    }
}
