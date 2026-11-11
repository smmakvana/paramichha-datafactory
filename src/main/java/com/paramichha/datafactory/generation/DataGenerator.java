package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;
import java.util.ArrayList;
import java.util.List;

/**
 * Never changes — add a new type by implementing {@link DataCreator}.
 */
public final class DataGenerator {
    private static final List<DataCreator> CREATORS = List.of(
            BooleanDataCreator.INSTANCE, // before String
            EnumDataCreator.INSTANCE, // before Object
            UuidDataCreator.INSTANCE, // before String
            TemporalDataCreator.INSTANCE, // before Object
            NumericDataCreator.INSTANCE, // before Object
            CharacterDataCreator.INSTANCE, // before String
            ArrayDataCreator.INSTANCE, // before Object
            CollectionDataCreator.INSTANCE, // before Object
            StringDataCreator.INSTANCE // catch-all
            );

    private DataGenerator() {}

    /**
     * Generates one value for the field at the given target.
     *
     * @throws com.paramichha.datafactory.DataFactoryException when no creator supports the field's type
     */
    public static Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        if ("null".equals(target.label())) {
            return null;
        }
        for (DataCreator creator : CREATORS) {
            if (creator.supports(field.fieldType())) {
                return creator.shape(field, target, machine);
            }
        }
        // #1 — no silent null: surface unsupported types immediately
        throw new com.paramichha.datafactory.DataFactoryException(
                com.paramichha.datafactory.ErrorType.UNSUPPORTED_CLASS_TYPE,
                "No DataCreator found for type: " + field.fieldType().getName() + " (field: " + field.fieldName()
                        + ")");
    }

    /**
     * Produces all values for a field — one per boundary target.
     * Enum CREATORS return a {@code List} of constants expanded inline. A List-typed field's
     * value is a list too, but it is one value and is kept whole.
     */
    public static List<Object> shapeAll(FieldConstraints field, List<BoundaryTarget> targets, DataMachine machine) {
        List<Object> results = new ArrayList<>();
        for (BoundaryTarget target : targets) {
            Object value = shape(field, target, machine);
            if (value instanceof List<?> list && field.fieldType().isEnum()) {
                results.addAll(list);
            } else {
                results.add(value);
            }
        }
        // #2 — @Null fields must yield null; only strip nulls for non-null-constrained fields
        if (!field.mustBeNull()) {
            results.removeIf(java.util.Objects::isNull);
        }
        // not List.copyOf — it rejects nulls, and @Null fields must yield null
        return java.util.Collections.unmodifiableList(results);
    }
}
