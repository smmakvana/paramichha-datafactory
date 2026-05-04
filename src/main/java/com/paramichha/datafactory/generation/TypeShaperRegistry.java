package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

import java.util.List;

/**
 * Holds all {@link DataCreator} implementations in priority order and delegates to the first match.
 *
 * <p>Order matters — more specific types must precede broader ones (e.g. Boolean before String).
 */
final class TypeShaperRegistry {

    static final TypeShaperRegistry INSTANCE = new TypeShaperRegistry();

    private final List<DataCreator> shapers = List.of(
            BooleanDataCreator.INSTANCE,   // before String
            EnumDataCreator.INSTANCE,      // before Object
            UuidDataCreator.INSTANCE,      // before String
            TemporalDataCreator.INSTANCE,  // before Object
            NumericDataCreator.INSTANCE,   // before Object
            CharacterDataCreator.INSTANCE, // before String
            StringDataCreator.INSTANCE     // catch-all
    );

    private TypeShaperRegistry() {
    }

    /**
     * Returns {@code null} if no shaper supports the field type.
     */
    Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        for (DataCreator shaper : shapers) {
            if (shaper.supports(field.fieldType())) {
                return shaper.shape(field, target, faker);
            }
        }
        return null;
    }
}
