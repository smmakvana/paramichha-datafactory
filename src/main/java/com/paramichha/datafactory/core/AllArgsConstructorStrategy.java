package com.paramichha.datafactory.core;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a class via an all-args constructor matched by parameter count or exact types.
 */
final class AllArgsConstructorStrategy implements InstantiationStrategy {

    public static final AllArgsConstructorStrategy INSTANCE = new AllArgsConstructorStrategy();

    private AllArgsConstructorStrategy() {
    }

    private static Constructor<?> findConstructor(Class<?> type, List<FieldDescriptor> fields) {
        try {
            return type.getDeclaredConstructor(
                    fields.stream().map(FieldDescriptor::rawType).toArray(Class[]::new));
        } catch (NoSuchMethodException ignored) {
        }
        return Arrays.stream(type.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == fields.size())
                .findFirst()
                .orElse(null);
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
        return findConstructor(type, fields) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                             Map<String, Object> values) throws Exception {
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
