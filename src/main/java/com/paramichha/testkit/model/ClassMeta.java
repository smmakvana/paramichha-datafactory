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
        List<MethodMeta> methods
) {
    public enum ConstructorStyle { LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER }

    /** Backward-compatible constructor for code that doesn't provide methods. */
    public ClassMeta(String simpleName, String packageName,
                     ConstructorStyle constructorStyle, List<FieldMeta> fields) {
        this(simpleName, packageName, constructorStyle, fields, List.of());
    }
}
