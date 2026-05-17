package com.paramichha.testkit.model;

import java.util.List;

public record ClassMeta(
        ConstructorStyle constructorStyle,
        List<FieldMeta> fields,
        List<MethodMeta> methods,
        boolean utilityClass,
        Class<?> type) {
    public enum ConstructorStyle { LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER }

    public ClassMeta(ConstructorStyle constructorStyle, List<FieldMeta> fields, Class<?> type) {
        this(constructorStyle, fields, List.of(), false, type);
    }

    public ClassMeta(ConstructorStyle constructorStyle, List<FieldMeta> fields, List<MethodMeta> methods, Class<?> type) {
        this(constructorStyle, fields, methods, false, type);
    }

    public String simpleName() {
        return type.getSimpleName();
    }

    public String packageName() {
        return type.getPackageName();
    }
}