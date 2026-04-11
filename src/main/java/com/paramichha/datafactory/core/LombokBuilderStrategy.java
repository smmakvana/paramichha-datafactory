package com.paramichha.datafactory.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Instantiates a class via its Lombok {@code @Builder} static {@code builder()} method.
 * {@code setAccessible(true)} is required for non-public inner classes.
 */
final class LombokBuilderStrategy implements InstantiationStrategy {

    public static final LombokBuilderStrategy INSTANCE = new LombokBuilderStrategy();

    private LombokBuilderStrategy() {
    }

    /**
     * Finds the builder setter by exact type first, then by name if that fails.
     * Lombok generates setters matching the declared field type, but for primitive
     * fields the setter may use the primitive type even if the field is declared as a wrapper.
     */
    private static Method findSetter(Class<?> builderClass, String name, Class<?> fieldType) {
        // Try exact match first
        try {
            return builderClass.getMethod(name, fieldType);
        } catch (NoSuchMethodException ignored) {
        }

        // Try the primitive equivalent if fieldType is a wrapper
        Class<?> primitive = toPrimitive(fieldType);
        if (primitive != null) {
            try {
                return builderClass.getMethod(name, primitive);
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Try the wrapper equivalent if fieldType is a primitive
        Class<?> wrapper = toWrapper(fieldType);
        if (wrapper != null) {
            try {
                return builderClass.getMethod(name, wrapper);
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Last resort: find by name only
        for (Method m : builderClass.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }

    /**
     * Coerces the value to the type the setter actually expects.
     * Handles the common case where castNumeric returns a long/int
     * but the setter expects BigInteger, BigDecimal, or another numeric type.
     */
    private static Object coerce(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;

        // Number -> Number coercions
        if (value instanceof Number n) {
            if (targetType == BigInteger.class || targetType == BigInteger.class)
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

            Method setter = findSetter(builderClass, f.name(), f.rawType());
            if (setter == null) continue;

            setter.setAccessible(true);
            setter.invoke(builder, coerce(value, setter.getParameterTypes()[0]));
        }

        Method buildMethod = builderClass.getMethod("build");
        buildMethod.setAccessible(true);
        return (T) buildMethod.invoke(builder);
    }
}
