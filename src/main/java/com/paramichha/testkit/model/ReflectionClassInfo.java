package com.paramichha.testkit.model;

import java.util.List;
import java.util.Objects;

public final class ReflectionClassInfo implements ClassInfo {
    private final ConstructorStyle constructorStyle;
    private final List<FieldInfo> fields;
    private final List<MethodInfo> methods;
    private final boolean utilityClass;
    private final Class<?> type;

    public ReflectionClassInfo(
            ConstructorStyle constructorStyle,
            List<FieldInfo> fields,
            List<MethodInfo> methods,
            boolean utilityClass,
            Class<?> type) {
        this.constructorStyle = constructorStyle;
        this.fields = fields;
        this.methods = methods;
        this.utilityClass = utilityClass;
        this.type = type;
    }

    public ReflectionClassInfo(ConstructorStyle constructorStyle, List<FieldInfo> fields, Class<?> type) {
        this(constructorStyle, fields, List.of(), false, type);
    }

    public ReflectionClassInfo(ConstructorStyle constructorStyle, List<FieldInfo> fields, List<MethodInfo> methods, Class<?> type) {
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

    @Override
    public ConstructorStyle constructorStyle() {
        return constructorStyle;
    }

    @Override
    public List<FieldInfo> fields() {
        return fields;
    }

    @Override
    public List<MethodInfo> methods() {
        return methods;
    }

    @Override
    public boolean utilityClass() {
        return utilityClass;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(constructorStyle, fields, methods, utilityClass, type);
    }

    @Override
    public String toString() {
        return "ReflectionClassInfo[" +
                "constructorStyle=" + constructorStyle + ", " +
                "fields=" + fields + ", " +
                "methods=" + methods + ", " +
                "utilityClass=" + utilityClass + ", " +
                "type=" + type + ']';
    }


}