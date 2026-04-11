package com.paramichha.datafactory.analyzer;

/**
 * Resolved quantity bounds for a field, derived by intersecting all quantity-related
 * annotations ({@code @Min}, {@code @Max}, {@code @Size}, {@code @Positive}, etc.).
 *
 * <p>A {@code null} min means unbounded below; a {@code null} max means unbounded above.
 * For strings the bounds represent length; for numbers they represent value.
 */
public record QuantityBounds(Long min, Long max) {

    public static QuantityBounds unbounded() {
        return new QuantityBounds(null, null);
    }

    public static QuantityBounds of(Long min, Long max) {
        return new QuantityBounds(min, max);
    }

    public static QuantityBounds atLeast(long min) {
        return new QuantityBounds(min, null);
    }

    public static QuantityBounds atMost(long max) {
        return new QuantityBounds(null, max);
    }

    public static QuantityBounds exactly(long value) {
        return new QuantityBounds(value, value);
    }

    private static Long maxOf(Long a, Long b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.max(a, b);
    }

    private static Long minOf(Long a, Long b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.min(a, b);
    }

    public boolean hasMin() {
        return min != null;
    }

    public boolean hasMax() {
        return max != null;
    }

    public boolean isUnbounded() {
        return min == null && max == null;
    }

    public boolean hasBoth() {
        return min != null && max != null;
    }

    public boolean isContradictory() {
        return min != null && max != null && min > max;
    }

    /**
     * Returns the tightest intersection: higher min, lower max.
     */
    public QuantityBounds intersect(QuantityBounds other) {
        return new QuantityBounds(maxOf(this.min, other.min), minOf(this.max, other.max));
    }
}
