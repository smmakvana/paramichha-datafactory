package com.paramichha.datafactory.shaper;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryTarget;

import java.util.List;

/**
 * Holds all {@link TypeShaper} implementations in priority order and delegates to the first match.
 *
 * <p>Order matters — more specific types must precede broader ones (e.g. Boolean before String).
 */
final class TypeShaperRegistry {

    static final TypeShaperRegistry INSTANCE = new TypeShaperRegistry();

    private final List<TypeShaper> shapers = List.of(
            BooleanTypeShaper.INSTANCE,   // before String
            EnumTypeShaper.INSTANCE,      // before Object
            UuidTypeShaper.INSTANCE,      // before String
            TemporalTypeShaper.INSTANCE,  // before Object
            NumericTypeShaper.INSTANCE,   // before Object
            CharacterTypeShaper.INSTANCE, // before String
            StringTypeShaper.INSTANCE     // catch-all
    );

    private TypeShaperRegistry() {
    }

    /**
     * Returns {@code null} if no shaper supports the field type.
     */
    Object shape(FieldConstraints field, BoundaryTarget target) {
        for (TypeShaper shaper : shapers) {
            if (shaper.supports(field.fieldType())) {
                return shaper.shape(field, target);
            }
        }
        return null;
    }
}
