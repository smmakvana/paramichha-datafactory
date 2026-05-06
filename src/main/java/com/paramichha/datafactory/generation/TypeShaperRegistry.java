package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import java.util.List;

/**
 * Holds all {@link DataCreator} implementations in priority order.
 * Delegates to the first creator that supports the field type.
 */
final class TypeShaperRegistry {
    static final TypeShaperRegistry INSTANCE = new TypeShaperRegistry();

    private final List<DataCreator> creators = List.of(
            BooleanDataCreator.INSTANCE,    // before String
            EnumDataCreator.INSTANCE,       // before Object
            UuidDataCreator.INSTANCE,       // before String
            TemporalDataCreator.INSTANCE,   // before Object
            NumericDataCreator.INSTANCE,    // before Object
            CharacterDataCreator.INSTANCE,  // before String
            StringDataCreator.INSTANCE      // catch-all
    );

    private TypeShaperRegistry() {}

    /** Returns {@code null} if no creator supports the field type. */
    Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        for (DataCreator creator : creators) {
            if (creator.supports(field.fieldType())) {
                return creator.shape(field, target, machine);
            }
        }
        return null;
    }
}
