package com.paramichha.testkit.model;

import java.util.List;

public record ClassInfo(
        ConstructorStyle constructorStyle,
        List<FieldInfo> fields,
        List<MethodInfo> methods,
        boolean utilityClass,
        Class<?> type) implements ClassMetaData {

    public ClassInfo(ConstructorStyle constructorStyle, List<FieldInfo> fields, Class<?> type) {
        this(constructorStyle, fields, List.of(), false, type);
    }

    public ClassInfo(ConstructorStyle constructorStyle, List<FieldInfo> fields, List<MethodInfo> methods, Class<?> type) {
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