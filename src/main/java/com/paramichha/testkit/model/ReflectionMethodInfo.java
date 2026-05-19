package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

public final class ReflectionMethodInfo {
    private final String name;
    private final ReflectionReturnTypeInfo returnType;
    private final List<ReflectionParameterInfo> parameters;
    private final MethodKind kind;

    public ReflectionMethodInfo(
            String name,
            ReflectionReturnTypeInfo returnType,
            List<ReflectionParameterInfo> parameters,
            MethodKind kind
    ) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.kind = kind;
    }

    /**
     * Backward-compatible — wraps raw type + annotations into ReturnTypeMeta.
     */
    public ReflectionMethodInfo(String name, Class<?> rawReturnType,
                                List<Annotation> returnAnnotations,
                                List<ReflectionParameterInfo> parameters, MethodKind kind) {
        this(name, new ReflectionReturnTypeInfo(rawReturnType, returnAnnotations), parameters, kind);
    }

    /**
     * Backward-compatible — no return annotations.
     */
    public ReflectionMethodInfo(String name, Class<?> rawReturnType,
                                List<ReflectionParameterInfo> parameters, MethodKind kind) {
        this(name, new ReflectionReturnTypeInfo(rawReturnType, List.of()), parameters, kind);
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public boolean hasReturnConstraints() {
        return returnType.isConstrained();
    }

    public String name() {
        return name;
    }

    public ReflectionReturnTypeInfo returnType() {
        return returnType;
    }

    public List<ReflectionParameterInfo> parameters() {
        return parameters;
    }

    public MethodKind kind() {
        return kind;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReflectionMethodInfo) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.returnType, that.returnType) &&
                Objects.equals(this.parameters, that.parameters) &&
                Objects.equals(this.kind, that.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, returnType, parameters, kind);
    }

    @Override
    public String toString() {
        return "ReflectionMethodInfo[" +
                "name=" + name + ", " +
                "returnType=" + returnType + ", " +
                "parameters=" + parameters + ", " +
                "kind=" + kind + ']';
    }

}