package com.paramichha.datafactory.utils;

/**
 * #14 — single source of truth for primitive default values.
 * Previously duplicated in RecordObjectCreation and ConstructorObjectCreation.
 */
public final class PrimitiveDefaults {

    private PrimitiveDefaults() {}

    public static Object of(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0f;
        }
        if (type == double.class) {
            return 0.0d;
        }
        return null;
    }
}
