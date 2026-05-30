package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

import java.util.ArrayList;
import java.util.List;

/**
 * Never changes — add a new type by implementing {@link DataCreator}.
 */
public final class DataGenerator {
    private static final List<DataCreator> creators = List.of(
            BooleanDataCreator.INSTANCE,    // before String
            EnumDataCreator.INSTANCE,       // before Object
            UuidDataCreator.INSTANCE,       // before String
            TemporalDataCreator.INSTANCE,   // before Object
            NumericDataCreator.INSTANCE,    // before Object
            CharacterDataCreator.INSTANCE,  // before String
            ArrayDataCreator.INSTANCE,      // before Object
            CollectionDataCreator.INSTANCE, // before Object
            StringDataCreator.INSTANCE      // catch-all
    );

    private DataGenerator() {
    }

    public static Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        if ("null".equals(target.label())) return null;
        for (DataCreator creator : creators) {
            if (creator.supports(field.fieldType())) {
                return creator.shape(field, target, machine);
            }
        }
        // #1 — no silent null: surface unsupported types immediately
        throw new com.paramichha.datafactory.DataFactoryException(
                "No DataCreator found for type: " + field.fieldType().getName()
                + " (field: " + field.fieldName() + ")");
    }

    /**
     * Produces all values for a field — one per boundary target.
     * Enum creators return a {@code List} of constants expanded inline.
     */
    public static List<Object> shapeAll(FieldConstraints field, List<BoundaryTarget> targets, DataMachine machine) {
        List<Object> results = new ArrayList<>();
        for (BoundaryTarget target : targets) {
            Object value = shape(field, target, machine);
            if (value instanceof List<?> list) {
                results.addAll(list);
            } else {
                results.add(value);
            }
        }
        // #2 — @Null fields must yield null; only strip nulls for non-null-constrained fields
        if (!field.mustBeNull()) {
            results.removeIf(java.util.Objects::isNull);
        }
        return List.copyOf(results);
    }
}
