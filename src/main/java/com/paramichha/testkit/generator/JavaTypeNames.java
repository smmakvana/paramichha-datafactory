package com.paramichha.testkit.generator;

/**
 * Java type name helpers for source code generation.
 * Shared type naming utility for source code generation.
 */
final class JavaTypeNames {

    private JavaTypeNames() {}

    /**
     * Returns the source-level type name, handling inner classes.
     * e.g. OuterClass$Inner → OuterClass.Inner
     */
    static String of(Class<?> type) {
        if (type.getEnclosingClass() != null)
            return type.getEnclosingClass().getSimpleName() + "." + type.getSimpleName();
        return type.getSimpleName();
    }

    /**
     * Returns the boxed type name for use in parameterized test signatures.
     * Primitives → their wrapper; everything else → of(type).
     */
    static String boxed(Class<?> type) {
        if (!type.isPrimitive()) return of(type);
        return switch (type.getName()) {
            case "boolean" -> "Boolean";
            case "byte"    -> "Byte";
            case "short"   -> "Short";
            case "int"     -> "Integer";
            case "long"    -> "Long";
            case "float"   -> "Float";
            case "double"  -> "Double";
            case "char"    -> "Character";
            default        -> of(type);
        };
    }

    /**
     * Returns a null literal with explicit cast — safe for Arrays.asList().
     * e.g. nullCast(UUID.class) → "(UUID) null"
     */
    static String nullCast(Class<?> type) {
        if (type.isPrimitive()) {
            return switch (type.getName()) {
                case "boolean" -> "false";
                case "byte"    -> "(byte) 0";
                case "short"   -> "(short) 0";
                case "long"    -> "0L";
                case "float"   -> "0.0f";
                case "double"  -> "0.0d";
                case "char"    -> "'\\0'";
                default        -> "0";
            };
        }
        return "(" + of(type) + ") null";
    }
}