package com.paramichha.datafactory.instantiation;

import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.utils.PrimitiveDefaults;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a class via an all-args constructor matched by parameter count or exact types.
 */
public final class ConstructorObjectCreation implements ObjectCreation {

    public static final ConstructorObjectCreation INSTANCE = new ConstructorObjectCreation();

    private ConstructorObjectCreation() {}

    private static Constructor<?> findConstructor(Class<?> type, List<FieldDescriptor> fields) {
        try {
            return type.getDeclaredConstructor(
                    fields.stream().map(FieldDescriptor::rawType).toArray(Class[]::new));
        } catch (NoSuchMethodException ignored) {
            // no exact match: look for a constructor with compatible parameter types below
        }
        return Arrays.stream(type.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == fields.size())
                .findFirst()
                .orElse(null);
    }

    private static Object defaultPrimitive(Class<?> type) {
        return PrimitiveDefaults.of(type); // #14 — shared utility, no duplication
    }

    @Override
    public boolean canCreate(Class<?> type, List<FieldDescriptor> fields) {
        return findConstructor(type, fields) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> type, List<FieldDescriptor> fields, Map<String, Object> values) throws Exception {
        Constructor<?> ctor = findConstructor(type, fields);
        ctor.setAccessible(true);
        Object[] args = fields.stream()
                .map(f -> {
                    Object v = values.get(f.name());
                    return (v == null && f.rawType().isPrimitive()) ? defaultPrimitive(f.rawType()) : v;
                })
                .toArray();
        return (T) ctor.newInstance(args);
    }
}
