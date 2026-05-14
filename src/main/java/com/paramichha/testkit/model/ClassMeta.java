package com.paramichha.testkit.model;

import java.util.List;

public record ClassMeta(
        String simpleName,
        String packageName,
        ConstructorStyle constructorStyle,
        List<FieldMeta> fields,
        List<MethodMeta> methods,
        boolean utilityClass
) {
    public enum ConstructorStyle { LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER }

    public ClassMeta(String simpleName, String packageName,
                     ConstructorStyle constructorStyle, List<FieldMeta> fields) {
        this(simpleName, packageName, constructorStyle, fields, List.of(), false);
    }

    public ClassMeta(String simpleName, String packageName,
                     ConstructorStyle constructorStyle, List<FieldMeta> fields,
                     List<MethodMeta> methods) {
        this(simpleName, packageName, constructorStyle, fields, methods, false);
    }
}