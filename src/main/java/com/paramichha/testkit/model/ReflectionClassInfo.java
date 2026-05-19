package com.paramichha.testkit.model;

import java.util.List;

public record ReflectionClassInfo(
        ConstructorStyle constructorStyle,
        List<ReflectionFieldInfo> fields,
        List<ReflectionMethodInfo> methods,
        boolean utilityClass,
        Class<?> type)  {

    public ReflectionClassInfo(ConstructorStyle constructorStyle, List<ReflectionFieldInfo> fields, Class<?> type) {
        this(constructorStyle, fields, List.of(), false, type);
    }

    public ReflectionClassInfo(ConstructorStyle constructorStyle, List<ReflectionFieldInfo> fields, List<ReflectionMethodInfo> methods, Class<?> type) {
        this(constructorStyle, fields, methods, false, type);
    }

    public String simpleName() {
        return type.getSimpleName();
    }

    public String packageName() {
        return type.getPackageName();
    }

    public enum ConstructorStyle {LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER}
}