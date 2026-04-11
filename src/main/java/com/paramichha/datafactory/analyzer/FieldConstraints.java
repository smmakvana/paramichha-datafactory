package com.paramichha.datafactory.analyzer;

import com.paramichha.datafactory.planner.BoundaryPlanner;
import com.paramichha.datafactory.shaper.ValueGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable snapshot of a field's resolved constraints.
 * Produced by {@link AnnotationAnalyzer}; consumed by
 * {@link BoundaryPlanner} and
 * {@link ValueGenerator}.
 */
public record FieldConstraints(
        String fieldName,
        Class<?> fieldType,
        boolean nullable,
        boolean mustBeNull,
        FormatType format,
        String patternRegexp,
        QuantityBounds bounds,
        boolean integerOnly,
        int maxIntegerDigits,
        int maxFractionDigits,
        TemporalDirection temporal,
        boolean assertTrueRequired,
        boolean assertFalseRequired,
        boolean cascadeValid,
        List<String> warnings
) {

    public static Builder builder(String fieldName, Class<?> fieldType) {
        return new Builder(fieldName, fieldType);
    }

    public boolean hasFormat() {
        return format != FormatType.NONE;
    }

    public boolean hasBounds() {
        return !bounds.isUnbounded();
    }

    public boolean hasTemporal() {
        return temporal != TemporalDirection.NONE;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean isPast() {
        return temporal == TemporalDirection.PAST || temporal == TemporalDirection.PAST_OR_NOW;
    }

    public boolean isFuture() {
        return temporal == TemporalDirection.FUTURE || temporal == TemporalDirection.FUTURE_OR_NOW;
    }

    public boolean isStrictPast() {
        return temporal == TemporalDirection.PAST;
    }

    public boolean isStrictFuture() {
        return temporal == TemporalDirection.FUTURE;
    }

    public static class Builder {
        private final String fieldName;
        private final Class<?> fieldType;
        private final List<String> warnings = new ArrayList<>();
        private boolean nullable = true;
        private boolean mustBeNull = false;
        private FormatType format = FormatType.NONE;
        private String patternRegexp = null;
        private QuantityBounds bounds = QuantityBounds.unbounded();
        private boolean integerOnly = false;
        private int maxIntegerDigits = Integer.MAX_VALUE;
        private int maxFractionDigits = Integer.MAX_VALUE;
        private TemporalDirection temporal = TemporalDirection.NONE;
        private boolean assertTrueRequired = false;
        private boolean assertFalseRequired = false;
        private boolean cascadeValid = false;

        private Builder(String fieldName, Class<?> fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }

        public Builder nullable(boolean v) {
            this.nullable = v;
            return this;
        }

        public Builder mustBeNull(boolean v) {
            this.mustBeNull = v;
            return this;
        }

        public Builder format(FormatType v) {
            this.format = v;
            return this;
        }

        public Builder patternRegexp(String v) {
            this.patternRegexp = v;
            return this;
        }

        public Builder bounds(QuantityBounds v) {
            this.bounds = v;
            return this;
        }

        public Builder integerOnly(boolean v) {
            this.integerOnly = v;
            return this;
        }

        public Builder maxIntegerDigits(int v) {
            this.maxIntegerDigits = v;
            return this;
        }

        public Builder maxFractionDigits(int v) {
            this.maxFractionDigits = v;
            return this;
        }

        public Builder temporal(TemporalDirection v) {
            this.temporal = v;
            return this;
        }

        public Builder assertTrueRequired(boolean v) {
            this.assertTrueRequired = v;
            return this;
        }

        public Builder assertFalseRequired(boolean v) {
            this.assertFalseRequired = v;
            return this;
        }

        public Builder cascadeValid(boolean v) {
            this.cascadeValid = v;
            return this;
        }

        public Builder warn(String msg) {
            this.warnings.add(msg);
            return this;
        }

        public FieldConstraints build() {
            return new FieldConstraints(
                    fieldName, fieldType,
                    nullable, mustBeNull,
                    format, patternRegexp,
                    bounds, integerOnly, maxIntegerDigits, maxFractionDigits,
                    temporal,
                    assertTrueRequired, assertFalseRequired,
                    cascadeValid,
                    List.copyOf(warnings));
        }
    }
}
