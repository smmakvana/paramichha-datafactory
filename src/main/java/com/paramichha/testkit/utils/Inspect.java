package com.paramichha.testkit.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class Inspect {

    private Inspect() {}

    public static List<Field> fields(Class<?> type) {
        return Arrays.asList(type.getDeclaredFields());
    }

    public static List<Method> methods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> !m.isSynthetic())
                .toList();
    }

    /**
     * Returns true for zero-param non-void methods starting with "get" or "is".
     * Handles both regular getters (getAge) and boolean getters (isActive).
     */
    public static boolean isGetter(Method method) {
        if (method.getParameterCount() != 0) return false;
        if (method.getReturnType() == void.class) return false;
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) return true;
        if (name.startsWith("is")  && name.length() > 2) return true;
        return false;
    }

    public static boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getName().length() > 3
                && method.getParameterCount() == 1
                && method.getReturnType() == void.class;
    }

    public static boolean isCollection(Class<?> type) {
        return java.util.Collection.class.isAssignableFrom(type);
    }

    public static boolean isMap(Class<?> type) {
        return java.util.Map.class.isAssignableFrom(type);
    }

    public static boolean isOptional(Class<?> type) {
        return java.util.Optional.class.isAssignableFrom(type);
    }

    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive();
    }

    public static boolean isEnum(Class<?> type) {
        return type.isEnum();
    }

    public static boolean isRecord(Class<?> type) {
        return type.isRecord();
    }

    public static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + type.getName(), e);
        }
    }
}
