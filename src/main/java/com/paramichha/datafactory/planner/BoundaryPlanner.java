package com.paramichha.datafactory.planner;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.analyzer.QuantityBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides which {@link BoundaryTarget}s to generate values for, given a field's constraints.
 *
 * <p>Rules:
 * <ul>
 *   <li>{@code mustBeNull}    → [null]
 *   <li>Enum                  → [allEnumValues]
 *   <li>Boolean               → [true/false], or just [true] / [false] with {@code @AssertTrue} / {@code @AssertFalse}
 *   <li>Temporal / UUID       → [semantic]
 *   <li>No bounds             → [semantic]
 *   <li>Min only              → [semantic, atMin, justAboveMin]
 *   <li>Max only              → [semantic, justBelowMax, atMax]
 *   <li>Both bounds           → [semantic, atMin, justAboveMin, midpoint, justBelowMax, atMax]
 *   <li>Min == Max            → [atMin]
 * </ul>
 *
 * <p>Duplicate targets (e.g. when min and max are adjacent) are removed while preserving order.
 */
public final class BoundaryPlanner {

    private BoundaryPlanner() {
    }

    public static List<BoundaryTarget> plan(FieldConstraints field) {
        if (field.mustBeNull()) return List.of(BoundaryTarget.nullTarget());
        if (field.fieldType().isEnum()) return List.of(BoundaryTarget.allEnumValues());
        if (isBoolean(field.fieldType())) return planBoolean(field);
        if (field.hasTemporal()) return List.of(BoundaryTarget.semantic());
        if (field.fieldType() == java.util.UUID.class) return List.of(BoundaryTarget.semantic());
        return planByBounds(field.bounds());
    }

    private static List<BoundaryTarget> planBoolean(FieldConstraints field) {
        if (field.assertTrueRequired()) return List.of(BoundaryTarget.trueTarget());
        if (field.assertFalseRequired()) return List.of(BoundaryTarget.falseTarget());
        return List.of(BoundaryTarget.trueTarget(), BoundaryTarget.falseTarget());
    }

    private static List<BoundaryTarget> planByBounds(QuantityBounds bounds) {
        if (bounds.isUnbounded()) return List.of(BoundaryTarget.semantic());

        Long min = bounds.min();
        Long max = bounds.max();

        if (min != null && max != null && min.equals(max)) {
            return List.of(BoundaryTarget.atMin(min));
        }

        List<BoundaryTarget> candidates = new ArrayList<>();
        candidates.add(BoundaryTarget.semantic());

        if (min != null && max != null) {
            candidates.add(BoundaryTarget.atMin(min));
            candidates.add(BoundaryTarget.justAboveMin(min));
            candidates.add(BoundaryTarget.midpoint(min, max));
            candidates.add(BoundaryTarget.justBelowMax(max));
            candidates.add(BoundaryTarget.atMax(max));
        } else if (min != null) {
            candidates.add(BoundaryTarget.atMin(min));
            candidates.add(BoundaryTarget.justAboveMin(min));
        } else {
            candidates.add(BoundaryTarget.justBelowMax(max));
            candidates.add(BoundaryTarget.atMax(max));
        }

        return deduplicate(candidates);
    }

    private static List<BoundaryTarget> deduplicate(List<BoundaryTarget> candidates) {
        List<BoundaryTarget> result = new ArrayList<>();
        java.util.Set<Long> seen = new java.util.LinkedHashSet<>();
        for (BoundaryTarget t : candidates) {
            if (t.targetQuantity() == null) {
                if (result.stream().noneMatch(BoundaryTarget::isSemantic)) result.add(t);
            } else {
                if (seen.add(t.targetQuantity())) result.add(t);
            }
        }
        return List.copyOf(result);
    }

    private static boolean isBoolean(Class<?> type) {
        return type == Boolean.class || type == boolean.class;
    }
}
