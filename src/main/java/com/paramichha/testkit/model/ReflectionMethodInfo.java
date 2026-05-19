package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

public final class ReflectionMethodInfo implements MethodInfo {
    private final String name;
    private final ReturnTypeInfo returnType;
    private final List<ParameterInfo> parameters;
    private final MethodKind kind;

    public ReflectionMethodInfo(
            String name,
            ReturnTypeInfo returnType,
            List<ParameterInfo> parameters,
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
                                List<ParameterInfo> parameters, MethodKind kind) {
        this(name, new ReflectionReturnTypeInfo(rawReturnType, returnAnnotations), parameters, kind);
    }

    /**
     * Backward-compatible — no return annotations.
     */
    public ReflectionMethodInfo(String name, Class<?> rawReturnType,
                                List<ParameterInfo> parameters, MethodKind kind) {
        this(name, new ReflectionReturnTypeInfo(rawReturnType, List.of()), parameters, kind);
    }

    @Override
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    @Override
    public boolean hasReturnConstraints() {
        return returnType.isConstrained();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ReturnTypeInfo returnType() {
        return returnType;
    }

    @Override
    public List<ParameterInfo> parameters() {
        return parameters;
    }

    @Override
    public MethodKind kind() {
        return kind;
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