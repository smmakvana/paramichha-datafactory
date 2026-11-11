package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.NumericBounds;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Decides which {@link BoundaryTarget}s to generate, given a field's constraints.
 *
 * <pre>
 *   mustBeNull             → [Special("null")]
 *   enum                   → [Special("allEnumValues")]
 *   boolean                → [Special("true"/"false")]
 *   temporal / UUID        → [Semantic]
 *   Unbounded + numeric    → [Fixed("negative",-1), Fixed("zero",0), Semantic]
 *   Unbounded + other      → [Semantic]
 *   IntegerBounds          → Fixed boundary targets
 *   DecimalBounds          → DecimalFixed boundary targets
 * </pre>
 */
public final class BoundaryPlanner {

    private BoundaryPlanner() {}

    /** The boundary targets to generate for a field: the edges of its constraints and a typical value. */
    public static List<BoundaryTarget> plan(FieldConstraints field) {
        if (field.mustBeNull()) {
            return List.of(BoundaryTarget.nullTarget());
        }
        if (field.fieldType().isEnum()) {
            return List.of(BoundaryTarget.allEnumValues());
        }
        if (isBoolean(field.fieldType())) {
            return planBoolean(field);
        }
        if (field.hasTemporal()) {
            return List.of(BoundaryTarget.semantic());
        }
        if (field.fieldType() == java.util.UUID.class) {
            return List.of(BoundaryTarget.semantic());
        }

        // Arrays and collections — use @Size bounds if present, else semantic
        if (field.fieldType().isArray()
                || java.util.Collection.class.isAssignableFrom(field.fieldType())
                || java.util.Map.class.isAssignableFrom(field.fieldType())) {
            return field.hasBounds() ? planCollectionBounds(field) : List.of(BoundaryTarget.semantic());
        }

        return switch (field.bounds()) {
            case NumericBounds.Unbounded u ->
                isNumeric(field.fieldType())
                        ? planTypeNaturalBounds(field.fieldType())
                        : List.of(BoundaryTarget.semantic());

            case NumericBounds.IntegerBounds b -> planInteger(b);
            case NumericBounds.DecimalBounds b -> planDecimal(b);
        };
    }

    // ── type-natural bounds (no @Min/@Max declared) ───────────────────────

    private static List<BoundaryTarget> planTypeNaturalBounds(Class<?> type) {
        if (type == Byte.class || type == byte.class) {
            return planInteger(new NumericBounds.IntegerBounds((long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE));
        }
        if (type == Short.class || type == short.class) {
            return planInteger(new NumericBounds.IntegerBounds((long) Short.MIN_VALUE, (long) Short.MAX_VALUE));
        }
        if (type == Integer.class || type == int.class) {
            return planInteger(new NumericBounds.IntegerBounds((long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE));
        }
        if (type == Long.class || type == long.class) {
            return planInteger(new NumericBounds.IntegerBounds(Long.MIN_VALUE, Long.MAX_VALUE));
        }
        // float/double/BigDecimal/BigInteger — semantic + zero + negative as before
        return List.of(
                BoundaryTarget.typeDefaultNegative(), BoundaryTarget.typeDefaultZero(), BoundaryTarget.semantic());
    }

    // ── boolean ───────────────────────────────────────────────────────────

    private static List<BoundaryTarget> planBoolean(FieldConstraints f) {
        if (f.assertTrueRequired()) {
            return List.of(BoundaryTarget.trueTarget());
        }
        if (f.assertFalseRequired()) {
            return List.of(BoundaryTarget.falseTarget());
        }
        return List.of(BoundaryTarget.trueTarget(), BoundaryTarget.falseTarget());
    }

    // ── integer bounds ────────────────────────────────────────────────────

    private static List<BoundaryTarget> planInteger(NumericBounds.IntegerBounds b) {
        Long min = b.min();
        Long max = b.max();

        if (min != null && min.equals(max)) {
            return List.of(BoundaryTarget.atMin(min));
        }

        List<BoundaryTarget> out = new ArrayList<>();
        out.add(BoundaryTarget.semantic());

        if (min != null && max != null) {
            out.add(BoundaryTarget.atMin(min));
            out.add(BoundaryTarget.justAboveMin(min));
            out.add(BoundaryTarget.midpoint(min, max));
            out.add(BoundaryTarget.justBelowMax(max));
            out.add(BoundaryTarget.atMax(max));
        } else if (min != null) {
            out.add(BoundaryTarget.atMin(min));
            out.add(BoundaryTarget.justAboveMin(min));
        } else {
            out.add(BoundaryTarget.justBelowMax(max));
            out.add(BoundaryTarget.atMax(max));
        }
        return deduplicateFixed(out);
    }

    // ── decimal bounds ────────────────────────────────────────────────────

    private static List<BoundaryTarget> planDecimal(NumericBounds.DecimalBounds b) {
        BigDecimal min = b.decMin();
        BigDecimal max = b.decMax();

        if (min != null && max != null && min.compareTo(max) == 0) {
            return List.of(BoundaryTarget.atDecimalMin(min));
        }

        List<BoundaryTarget> out = new ArrayList<>();
        out.add(BoundaryTarget.semantic());

        if (min != null && max != null) {
            out.add(BoundaryTarget.atDecimalMin(min));
            out.add(BoundaryTarget.justAboveDecimalMin(min));
            out.add(BoundaryTarget.decimalMidpoint(min, max));
            out.add(BoundaryTarget.justBelowDecimalMax(max));
            out.add(BoundaryTarget.atDecimalMax(max));
        } else if (min != null) {
            out.add(BoundaryTarget.atDecimalMin(min));
            out.add(BoundaryTarget.justAboveDecimalMin(min));
        } else {
            out.add(BoundaryTarget.justBelowDecimalMax(max));
            out.add(BoundaryTarget.atDecimalMax(max));
        }
        return deduplicateDecimal(out);
    }

    // ── deduplication ─────────────────────────────────────────────────────

    /** Keeps the first target per value: close bounds (e.g. 1..3) put several targets on one value. */
    private static List<BoundaryTarget> deduplicateFixed(List<BoundaryTarget> in) {
        Set<Long> seenValues = new HashSet<>();
        return in.stream()
                .filter(t -> !(t instanceof BoundaryTarget.Fixed f) || seenValues.add(f.value()))
                .toList();
    }

    private static List<BoundaryTarget> deduplicateDecimal(List<BoundaryTarget> in) {
        List<BoundaryTarget> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (BoundaryTarget t : in) {
            String key = switch (t) {
                case BoundaryTarget.Semantic s -> "semantic";
                case BoundaryTarget.DecimalFixed d -> d.value().toPlainString();
                default -> t.label();
            };
            if (seen.add(key)) {
                out.add(t);
            }
        }
        return List.copyOf(out);
    }

    // ── type classification ───────────────────────────────────────────────

    private static boolean isBoolean(Class<?> t) {
        return t == Boolean.class || t == boolean.class;
    }

    private static boolean isNumeric(Class<?> t) {
        return t == Integer.class
                || t == int.class
                || t == Long.class
                || t == long.class
                || t == Short.class
                || t == short.class
                || t == Byte.class
                || t == byte.class
                || t == Float.class
                || t == float.class
                || t == Double.class
                || t == double.class
                || t == java.math.BigDecimal.class
                || t == java.math.BigInteger.class;
    }

    // ── Collection/array size planning ─────────────────────────────────

    private static List<BoundaryTarget> planCollectionBounds(FieldConstraints field) {
        if (!(field.bounds() instanceof NumericBounds.IntegerBounds b)) {
            return List.of(BoundaryTarget.semantic());
        }
        List<BoundaryTarget> targets = new ArrayList<>();
        if (field.nullable()) {
            targets.add(BoundaryTarget.nullTarget());
        }
        // #12 — b.min() and b.max() can be null (e.g. only max declared); guard both
        if (b.min() != null) {
            targets.add(BoundaryTarget.atMin(b.min()));
        }
        targets.add(BoundaryTarget.semantic());
        // Valid targets only: max + 1 violates @Size and belongs to the invalid cases.
        if (b.max() != null) {
            targets.add(BoundaryTarget.atMax(b.max()));
        }
        return List.copyOf(targets);
    }
}
