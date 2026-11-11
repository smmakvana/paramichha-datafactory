package com.paramichha.datafactory.instantiation;

import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.utils.PrimitiveDefaults;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a Java Record via its canonical constructor.
 */
public final class RecordObjectCreation implements ObjectCreation {

    public static final RecordObjectCreation INSTANCE = new RecordObjectCreation();

    private RecordObjectCreation() {}

    private static Object defaultPrimitive(Class<?> type) {
        return PrimitiveDefaults.of(type); // #14 — shared utility, no duplication
    }

    @Override
    public boolean canCreate(Class<?> type, List<FieldDescriptor> fields) {
        return type.isRecord();
    }

    @Override
    public <T> T create(Class<T> type, List<FieldDescriptor> fields, Map<String, Object> values) throws Exception {
        java.lang.reflect.RecordComponent[] components = type.getRecordComponents();
        Class<?>[] paramTypes = Arrays.stream(components)
                .map(java.lang.reflect.RecordComponent::getType)
                .toArray(Class[]::new);
        Object[] args = Arrays.stream(components)
                .map(c -> {
                    Object v = values.get(c.getName());
                    return (v == null && c.getType().isPrimitive()) ? defaultPrimitive(c.getType()) : v;
                })
                .toArray();
        var ctor = type.getDeclaredConstructor(paramTypes);
        ctor.setAccessible(true);
        return ctor.newInstance(args);
    }
}
