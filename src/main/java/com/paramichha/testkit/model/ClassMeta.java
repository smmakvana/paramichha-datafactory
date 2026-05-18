package com.paramichha.testkit.model;

import java.util.List;

public record ClassMeta(
        ConstructorStyle constructorStyle,
        List<FieldMeta> fields,
        List<MethodMeta> methods,
        boolean utilityClass,
        Class<?> type) implements ClassMetaData {

    public ClassMeta(ConstructorStyle constructorStyle, List<FieldMeta> fields, Class<?> type) {
        this(constructorStyle, fields, List.of(), false, type);
    }

    public ClassMeta(ConstructorStyle constructorStyle, List<FieldMeta> fields, List<MethodMeta> methods, Class<?> type) {
        this(constructorStyle, fields, methods, false, type);
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