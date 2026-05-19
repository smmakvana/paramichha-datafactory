package com.paramichha.testkit.model;

import java.util.List;
import java.util.Objects;

public final class ReflectionClassInfo {
    private final ConstructorStyle constructorStyle;
    private final List<ReflectionFieldInfo> fields;
    private final List<ReflectionMethodInfo> methods;
    private final boolean utilityClass;
    private final Class<?> type;

    public ReflectionClassInfo(
            ConstructorStyle constructorStyle,
            List<ReflectionFieldInfo> fields,
            List<ReflectionMethodInfo> methods,
            boolean utilityClass,
            Class<?> type) {
        this.constructorStyle = constructorStyle;
        this.fields = fields;
        this.methods = methods;
        this.utilityClass = utilityClass;
        this.type = type;
    }

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

    public ConstructorStyle constructorStyle() {
        return constructorStyle;
    }

    public List<ReflectionFieldInfo> fields() {
        return fields;
    }

    public List<ReflectionMethodInfo> methods() {
        return methods;
    }

    public boolean utilityClass() {
        return utilityClass;
    }

    public Class<?> type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReflectionClassInfo) obj;
        return Objects.equals(this.constructorStyle, that.constructorStyle) &&
                Objects.equals(this.fields, that.fields) &&
                Objects.equals(this.methods, that.methods) &&
                this.utilityClass == that.utilityClass &&
                Objects.equals(this.type, that.type);
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


    public enum ConstructorStyle {LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER}
}