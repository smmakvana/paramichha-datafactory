package com.paramichha.testkit.model;

import java.util.List;

public record ClassMeta(
        String simpleName,
        String packageName,
        ConstructorStyle constructorStyle,
        List<FieldMeta> fields,
        List<MethodMeta> methods,
        boolean utilityClass,
        Class<?> type) {
    public enum ConstructorStyle { LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER }

    public ClassMeta(String simpleName, String packageName, ConstructorStyle constructorStyle, List<FieldMeta> fields,Class<?> type) {
        this(simpleName, packageName, constructorStyle, fields, List.of(), false, type);
    }

    public ClassMeta(String simpleName, String packageName, ConstructorStyle constructorStyle, List<FieldMeta> fields, List<MethodMeta> methods, Class<?> type) {
        this(simpleName, packageName, constructorStyle, fields, methods, false, type);
    }

    @Override
    public String simpleName() {
        return type.getSimpleName();
    }

    @Override
    public String packageName() {
        return type.getPackageName();
    }
}