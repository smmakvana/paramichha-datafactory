package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

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
    default Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker);

}
