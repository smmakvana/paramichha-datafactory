package com.paramichha.datafactory.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a class via a no-args constructor, then populates fields via setters or direct access.
 */
final class SetterStrategy implements InstantiationStrategy {

    public static final SetterStrategy INSTANCE = new SetterStrategy();

    private SetterStrategy() {
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    @Override
    public boolean canHandle(Class<?> type, List<FieldDescriptor> fields) {
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                             Map<String, Object> values) throws Exception {
        Constructor<?> noArgs = type.getDeclaredConstructor();
        noArgs.setAccessible(true);
        T instance = (T) noArgs.newInstance();

        for (FieldDescriptor f : fields) {
            Object value = values.get(f.name());
            if (value == null && f.rawType().isPrimitive()) continue;

            String setter = "set" + Character.toUpperCase(f.name().charAt(0)) + f.name().substring(1);
            try {
                var m = type.getMethod(setter, f.rawType());
                m.setAccessible(true);
                m.invoke(instance, value);
            } catch (NoSuchMethodException e) {
                Field field = findField(type, f.name());
                if (field != null) {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
            }
        }
        return instance;
    }
}
