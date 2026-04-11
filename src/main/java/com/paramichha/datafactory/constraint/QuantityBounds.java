package com.paramichha.datafactory.constraint;

import java.math.BigDecimal;

/**
 * Resolved quantity bounds for a field — sealed to enforce exhaustive handling.
 *
 * <ul>
 *   <li>{@link Unbounded}     — no bounds at all
 *   <li>{@link IntegerBounds} — integer precision (@Min/@Max/@Size etc.)
 *   <li>{@link DecimalBounds} — full decimal precision (@DecimalMin/@DecimalMax)
 * </ul>
 *
 * Convenience interface methods ({@code min()}, {@code max()}, {@code hasMin()} etc.)
 * allow callers to query bounds without pattern matching.
 * {@link DecimalBounds} exposes its raw BigDecimal values via {@link DecimalBounds#decMin()}
 * and {@link DecimalBounds#decMax()} to avoid a return-type conflict with the Long
 * convenience methods on the interface.
 */
public sealed interface QuantityBounds
        permits QuantityBounds.Unbounded,
                QuantityBounds.IntegerBounds,
                QuantityBounds.DecimalBounds {

    // ── variants ──────────────────────────────────────────────────────────

    record Unbounded() implements QuantityBounds {}

    record IntegerBounds(Long min, Long max) implements QuantityBounds {
        public boolean hasMin()          { return min != null; }
        public boolean hasMax()          { return max != null; }
        public boolean hasBoth()         { return min != null && max != null; }
        public boolean isContradictory() { return min != null && max != null && min > max; }
    }

    record DecimalBounds(BigDecimal decMin, BigDecimal decMax) implements QuantityBounds {
        public boolean hasMin()          { return decMin != null; }
        public boolean hasMax()          { return decMax != null; }
        public boolean hasBoth()         { return decMin != null && decMax != null; }
        public boolean isContradictory() {
            return decMin != null && decMax != null && decMin.compareTo(decMax) > 0;
        }
    }

    // ── interface-level convenience accessors ─────────────────────────────

    default Long min() {
        return switch (this) {
            case IntegerBounds b -> b.min();
            case DecimalBounds b -> b.decMin() != null ? b.decMin().longValue() : null;
            case Unbounded ignored -> null;
        };
    }

    default Long max() {
        return switch (this) {
            case IntegerBounds b -> b.max();
            case DecimalBounds b -> b.decMax() != null ? b.decMax().longValue() : null;
            case Unbounded ignored -> null;
        };
    }

    default boolean hasMin()       { return min() != null; }
    default boolean hasMax()       { return max() != null; }
    default boolean hasBoth()      { return min() != null && max() != null; }
    default boolean isUnbounded()  { return this instanceof Unbounded; }

    default boolean isContradictory() {
        return switch (this) {
            case IntegerBounds b -> b.isContradictory();
            case DecimalBounds b -> b.isContradictory();
            case Unbounded ignored -> false;
        };
    }

    // ── factories ─────────────────────────────────────────────────────────

    static QuantityBounds unbounded()            { return new Unbounded(); }
    static QuantityBounds of(Long min, Long max) { return new IntegerBounds(min, max); }
    static QuantityBounds atLeast(long min)      { return new IntegerBounds(min, null); }
    static QuantityBounds atMost(long max)       { return new IntegerBounds(null, max); }
    static QuantityBounds exactly(long v)        { return new IntegerBounds(v, v); }

    static QuantityBounds atLeastDecimal(BigDecimal min) { return new DecimalBounds(min, null); }
    static QuantityBounds atMostDecimal(BigDecimal max)  { return new DecimalBounds(null, max); }

    // ── intersection ─────────────────────────────────────────────────────

    default QuantityBounds intersect(QuantityBounds other) {
        return switch (this) {
            case Unbounded ignored -> other;
            case IntegerBounds a -> switch (other) {
                case Unbounded ignored -> a;
                case IntegerBounds b   -> new IntegerBounds(maxOf(a.min(), b.min()), minOf(a.max(), b.max()));
                case DecimalBounds b   -> promoteAndIntersect(a, b);
            };
            case DecimalBounds a -> switch (other) {
                case Unbounded ignored -> a;
                case IntegerBounds b   -> promoteAndIntersect(b, a);
                case DecimalBounds b   -> new DecimalBounds(maxOfDec(a.decMin(), b.decMin()), minOfDec(a.decMax(), b.decMax()));
            };
        };
    }

    // ── private helpers ───────────────────────────────────────────────────

    private static QuantityBounds promoteAndIntersect(IntegerBounds i, DecimalBounds d) {
        BigDecimal dMin = d.decMin() != null ? d.decMin() : (i.min() != null ? BigDecimal.valueOf(i.min()) : null);
        BigDecimal dMax = d.decMax() != null ? d.decMax() : (i.max() != null ? BigDecimal.valueOf(i.max()) : null);
        if (i.min() != null && d.decMin() != null) dMin = maxOfDec(BigDecimal.valueOf(i.min()), d.decMin());
        if (i.max() != null && d.decMax() != null) dMax = minOfDec(BigDecimal.valueOf(i.max()), d.decMax());
        return new DecimalBounds(dMin, dMax);
    }

    private static Long maxOf(Long a, Long b) {
        if (a == null) return b; if (b == null) return a; return Math.max(a, b);
    }
    private static Long minOf(Long a, Long b) {
        if (a == null) return b; if (b == null) return a; return Math.min(a, b);
    }
    private static BigDecimal maxOfDec(BigDecimal a, BigDecimal b) {
        if (a == null) return b; if (b == null) return a; return a.compareTo(b) >= 0 ? a : b;
    }
    private static BigDecimal minOfDec(BigDecimal a, BigDecimal b) {
        if (a == null) return b; if (b == null) return a; return a.compareTo(b) <= 0 ? a : b;
    }
}
