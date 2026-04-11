package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

/**
 * Produces a realistic value for one field at one boundary target.
 *
 * <p>Implementations: {@link StringTypeShaper}, {@link NumericTypeShaper},
 * {@link TemporalTypeShaper}, {@link BooleanTypeShaper}, {@link EnumTypeShaper},
 * {@link UuidTypeShaper}.
 */
interface TypeShaper {

    /**
     * Returns {@code true} if this shaper handles the given field type.
     */
    boolean supports(Class<?> fieldType);

    /**
     * Produces one realistic value satisfying the field's constraints at the given target.
     */
    Object shape(FieldConstraints field, BoundaryTarget target);
}
