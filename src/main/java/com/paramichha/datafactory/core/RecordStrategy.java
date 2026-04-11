package com.paramichha.datafactory.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a Java Record via its canonical constructor.
 */
final class RecordStrategy implements InstantiationStrategy {

    public static final RecordStrategy INSTANCE = new RecordStrategy();

    private RecordStrategy() {
    }

    private static Object defaultPrimitive(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return null;
    }

    @Override
    public boolean canHandle(Class<?> type, List<FieldDescriptor> fields) {
        return type.isRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                             Map<String, Object> values) throws Exception {
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
        return (T) ctor.newInstance(args);
    }
}
