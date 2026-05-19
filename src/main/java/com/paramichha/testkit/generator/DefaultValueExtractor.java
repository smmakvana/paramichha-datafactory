package com.paramichha.testkit.generator;

import com.paramichha.testkit.model.FieldInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts per-field values from an object using standard Java reflection.
 */
final class DefaultValueExtractor implements ValueExtractor {

    static final DefaultValueExtractor INSTANCE = new DefaultValueExtractor();

    private DefaultValueExtractor() {}

    @Override
    public List<Object> extract(Object instance, Class<?> type, List<FieldInfo> fields) {
        if (instance == null)
            return fields.stream().map(f -> (Object) null).toList();

        List<Object> values = new ArrayList<>();
        for (FieldInfo field : fields) {
            try {
                if (type.isRecord()) {
                    values.add(type.getDeclaredMethod(field.name()).invoke(instance));
                } else {
                    Field f = findField(type, field.name());
                    if (f != null) { f.setAccessible(true); values.add(f.get(instance)); }
                    else values.add(null);
                }
            } catch (Exception e) {
                values.add(null);
            }
        }
        return values;
    }

    static Field findField(Class<?> cls, String name) {
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }
}
