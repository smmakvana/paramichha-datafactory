package com.paramichha.datafactory.generation;

import java.math.BigDecimal;

/**
 * A single target point for value generation — sealed to enforce exhaustive handling.
 *
 * <ul>
 *   <li>{@link Semantic}      — let the shaper decide semantically (field-name hints, formulas)
 *   <li>{@link Fixed}         — exact integer/long value (integer fields, string lengths)
 *   <li>{@link DecimalFixed}  — exact decimal value (BigDecimal/Double/Float fields)
 *   <li>{@link Special}       — null, true, false, allEnumValues
 * </ul>
 */
public sealed interface BoundaryTarget
        permits BoundaryTarget.Semantic,
                BoundaryTarget.Fixed,
                BoundaryTarget.DecimalFixed,
                BoundaryTarget.Special {

    // ── variants ──────────────────────────────────────────────────────────

    record Semantic(String label) implements BoundaryTarget {}

    record Fixed(String label, long value) implements BoundaryTarget {}

    record DecimalFixed(String label, BigDecimal value) implements BoundaryTarget {}

    record Special(String label) implements BoundaryTarget {}

    // ── common queries ────────────────────────────────────────────────────

    default String label() {
        return switch (this) {
            case Semantic s     -> s.label();
            case Fixed f        -> f.label();
            case DecimalFixed d -> d.label();
            case Special s      -> s.label();
        };
    }

    default Long targetQuantity() {
        return switch (this) {
            case Fixed f        -> f.value();
            case DecimalFixed d -> d.value().longValue();
            default             -> null;
        };
    }

    default boolean isSemantic() { return this instanceof Semantic; }
    default boolean isFixed()    { return this instanceof Fixed || this instanceof DecimalFixed; }

    // ── integer factories ─────────────────────────────────────────────────

    static BoundaryTarget semantic()             { return new Semantic("semantic"); }
    static BoundaryTarget typeDefaultNegative()  { return new Fixed("negative", -1L); }
    static BoundaryTarget typeDefaultZero()      { return new Fixed("zero", 0L); }

    static BoundaryTarget atMin(long min)        { return new Fixed("atMin", min); }
    static BoundaryTarget justAboveMin(long min) { return new Fixed("justAboveMin", min + 1); }
    static BoundaryTarget midpoint(long mn, long mx) { return new Fixed("midpoint", (mn + mx) / 2); }
    static BoundaryTarget justBelowMax(long max) { return new Fixed("justBelowMax", max - 1); }
    static BoundaryTarget atMax(long max)        { return new Fixed("atMax", max); }

    // ── decimal factories ─────────────────────────────────────────────────

    static BoundaryTarget atDecimalMin(BigDecimal min) {
        return new DecimalFixed("atMin", min);
    }

    static BoundaryTarget justAboveDecimalMin(BigDecimal min) {
        BigDecimal step = BigDecimal.ONE.scaleByPowerOfTen(-min.scale());
        return new DecimalFixed("justAboveMin", min.add(step));
    }

    static BoundaryTarget decimalMidpoint(BigDecimal mn, BigDecimal mx) {
        BigDecimal mid = mn.add(mx).divide(BigDecimal.TWO, mn.scale(), java.math.RoundingMode.HALF_UP);
        return new DecimalFixed("midpoint", mid);
    }

    static BoundaryTarget justBelowDecimalMax(BigDecimal max) {
        BigDecimal step = BigDecimal.ONE.scaleByPowerOfTen(-max.scale());
        return new DecimalFixed("justBelowMax", max.subtract(step));
    }

    static BoundaryTarget atDecimalMax(BigDecimal max) {
        return new DecimalFixed("atMax", max);
    }

    // ── special factories ─────────────────────────────────────────────────

    static BoundaryTarget nullTarget()    { return new Special("null"); }
    static BoundaryTarget trueTarget()    { return new Special("true"); }
    static BoundaryTarget falseTarget()   { return new Special("false"); }
    static BoundaryTarget allEnumValues() { return new Special("allEnumValues"); }
}
