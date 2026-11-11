package com.paramichha.datafactory.instantiation;

import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.utils.JavaNames;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a class via a no-args constructor, then populates fields via setters or direct access.
 */
public final class PojoObjectCreation implements ObjectCreation {

    public static final PojoObjectCreation INSTANCE = new PojoObjectCreation();

    private PojoObjectCreation() {}

    private static Field findField(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // not declared here: look in the superclass
            }
        }
        return null;
    }

    // Called for its NoSuchMethodException: the question is whether the constructor exists.
    @SuppressWarnings("PMD.UselessPureMethodCall")
    @Override
    public boolean canCreate(Class<?> type, List<FieldDescriptor> fields) {
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> type, List<FieldDescriptor> fields, Map<String, Object> values) throws Exception {
        Constructor<?> noArgs = type.getDeclaredConstructor();
        noArgs.setAccessible(true);
        T instance = (T) noArgs.newInstance();

        for (FieldDescriptor f : fields) {
            Object value = values.get(f.name());
            if (value == null && f.rawType().isPrimitive()) {
                continue;
            }

            String setter = JavaNames.setterName(f.name());
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
