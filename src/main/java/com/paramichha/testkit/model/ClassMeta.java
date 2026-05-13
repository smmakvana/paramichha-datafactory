package com.paramichha.testkit.model;

import java.util.List;

/**
 * Structural metadata about a class — style, fields, methods, names.
 * Produced by {@link com.paramichha.testkit.ClassReader}.
 */
public record ClassMeta(
        String simpleName,
        String packageName,
        ConstructorStyle constructorStyle,
        List<FieldMeta> fields,
        List<MethodMeta> methods,
        boolean utilityClass
) {
    public enum ConstructorStyle {LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER}

    /**
     * Backward-compatible constructor — not a utility class.
     */
    public ClassMeta(String simpleName, String packageName,
                     ConstructorStyle constructorStyle, List<FieldMeta> fields) {
        this(simpleName, packageName, constructorStyle, fields, List.of(), false);
    }

    /**
     * Backward-compatible constructor with methods.
     */
    public ClassMeta(String simpleName, String packageName,
                     ConstructorStyle constructorStyle, List<FieldMeta> fields,
                     List<MethodMeta> methods) {
        this(simpleName, packageName, constructorStyle, fields, methods, false);
    }
}