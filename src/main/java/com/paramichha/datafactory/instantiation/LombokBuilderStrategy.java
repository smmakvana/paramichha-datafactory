package com.paramichha.datafactory.instantiation;

import com.paramichha.datafactory.constraint.FieldDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instantiates a class via its Lombok {@code @Builder} static {@code builder()} method.
 * {@code setAccessible(true)} is required for non-public inner classes.
 */
public final class LombokBuilderStrategy implements InstantiationStrategy {

    public static final LombokBuilderStrategy INSTANCE = new LombokBuilderStrategy();

    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Optional<Method>>>
            SETTER_CACHE = new ConcurrentHashMap<>();

    private LombokBuilderStrategy() {
    }

    private static Method findSetter(Class<?> builderClass, String name, Class<?> fieldType) {
        try {
            return builderClass.getMethod(name, fieldType);
        } catch (NoSuchMethodException ignored) {
        }

        Class<?> primitive = toPrimitive(fieldType);
        if (primitive != null) {
            try {
                return builderClass.getMethod(name, primitive);
            } catch (NoSuchMethodException ignored) {
            }
        }

        Class<?> wrapper = toWrapper(fieldType);
        if (wrapper != null) {
            try {
                return builderClass.getMethod(name, wrapper);
            } catch (NoSuchMethodException ignored) {
            }
        }

        for (Method m : builderClass.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }

    private static Object coerce(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;

        if (value instanceof Number n) {
            if (targetType == BigInteger.class)
                return BigInteger.valueOf(n.longValue());
            if (targetType == BigDecimal.class)
                return BigDecimal.valueOf(n.doubleValue());
            if (targetType == Long.class || targetType == long.class)
                return n.longValue();
            if (targetType == Integer.class || targetType == int.class)
                return n.intValue();
            if (targetType == Short.class || targetType == short.class)
                return n.shortValue();
            if (targetType == Byte.class || targetType == byte.class)
                return n.byteValue();
            if (targetType == Double.class || targetType == double.class)
                return n.doubleValue();
            if (targetType == Float.class || targetType == float.class)
                return n.floatValue();
        }

        return value;
    }

    private static Class<?> toPrimitive(Class<?> wrapper) {
        if (wrapper == Boolean.class) return boolean.class;
        if (wrapper == Byte.class) return byte.class;
        if (wrapper == Short.class) return short.class;
        if (wrapper == Integer.class) return int.class;
        if (wrapper == Long.class) return long.class;
        if (wrapper == Float.class) return float.class;
        if (wrapper == Double.class) return double.class;
        if (wrapper == Character.class) return char.class;
        return null;
    }

    private static Class<?> toWrapper(Class<?> primitive) {
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == byte.class) return Byte.class;
        if (primitive == short.class) return Short.class;
        if (primitive == int.class) return Integer.class;
        if (primitive == long.class) return Long.class;
        if (primitive == float.class) return Float.class;
        if (primitive == double.class) return Double.class;
        if (primitive == char.class) return Character.class;
        return null;
    }

    @Override
    public boolean canHandle(Class<?> type, List<FieldDescriptor> fields) {
        try {
            return Modifier.isStatic(type.getDeclaredMethod("builder").getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                             Map<String, Object> values) throws Exception {
        Method builderMethod = type.getDeclaredMethod("builder");
        builderMethod.setAccessible(true);
        Object builder = builderMethod.invoke(null);
        Class<?> builderClass = builder.getClass();

        for (FieldDescriptor f : fields) {
            Object value = values.get(f.name());
            if (value == null && f.rawType().isPrimitive()) continue;

            Method setter = SETTER_CACHE
                    .computeIfAbsent(builderClass, c -> new ConcurrentHashMap<>())
                    .computeIfAbsent(f.name(), n -> Optional.ofNullable(
                            findSetter(builderClass, n, f.rawType())))
                    .orElse(null);
            if (setter == null) continue;

            setter.setAccessible(true);
            setter.invoke(builder, coerce(value, setter.getParameterTypes()[0]));
        }

        Method buildMethod = builderClass.getMethod("build");
        buildMethod.setAccessible(true);
        return (T) buildMethod.invoke(builder);
    }
}