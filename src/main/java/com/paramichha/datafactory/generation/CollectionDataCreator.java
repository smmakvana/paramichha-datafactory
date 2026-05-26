package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Produces {@code List}, {@code Set}, and {@code Map} values.
 *
 * <p>Returns typed empty or small collections. Size driven by {@code @Size} if present.
 * Element generation delegates to {@link DataGenerator} with {@code String} as component type
 * (collections without generic info at runtime use String as a safe default).
 */
final class CollectionDataCreator implements DataCreator {

    static final CollectionDataCreator INSTANCE = new CollectionDataCreator();

    private CollectionDataCreator() {}

    @Override
    public boolean supports(Class<?> type) {
        return type != null
                && (java.util.Collection.class.isAssignableFrom(type)
                || java.util.Map.class.isAssignableFrom(type));
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine) {
        if ("null".equals(target.label())) return null;

        Class<?> type = field.fieldType();

        // Size from bounds or default 2
        int size = 2;
        if (field.hasBounds()) {
            long qty = target.targetQuantity() != null ? target.targetQuantity() : 2L;
            size = (int) Math.max(0, Math.min(qty, 10));
        }

        // Map types
        if (java.util.Map.class.isAssignableFrom(type)) {
            Map<Object, Object> map = new HashMap<>();
            FieldConstraints keyField = FieldConstraints.builder(field.fieldName() + "Key", String.class).build();
            FieldConstraints valField = FieldConstraints.builder(field.fieldName() + "Value", String.class).build();
            for (int i = 0; i < size; i++) {
                Object k = DataGenerator.shape(keyField, BoundaryTarget.semantic(), machine);
                Object v = DataGenerator.shape(valField, BoundaryTarget.semantic(), machine);
                if (k != null && v != null) map.put(k, v);
            }
            return map;
        }

        // Set types
        if (java.util.Set.class.isAssignableFrom(type)) {
            Set<Object> set = new HashSet<>();
            FieldConstraints elemField = FieldConstraints.builder(field.fieldName(), String.class).build();
            for (int i = 0; i < size; i++) {
                Object elem = DataGenerator.shape(elemField, BoundaryTarget.semantic(), machine);
                if (elem != null) set.add(elem);
            }
            return set;
        }

        // List (default collection)
        List<Object> list = new ArrayList<>();
        FieldConstraints elemField = FieldConstraints.builder(field.fieldName(), String.class).build();
        for (int i = 0; i < size; i++) {
            Object elem = DataGenerator.shape(elemField, BoundaryTarget.semantic(), machine);
            if (elem != null) list.add(elem);
        }
        return list;
    }
}
