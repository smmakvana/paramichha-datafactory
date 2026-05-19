package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

public record MethodInfo(
        String name,
        ReturnInfo returnType,
        List<ParameterInfo> parameters,
        MethodKind kind
) {
    /** Backward-compatible — wraps raw type + annotations into ReturnTypeMeta. */
    public MethodInfo(String name, Class<?> rawReturnType,
                      List<Annotation> returnAnnotations,
                      List<ParameterInfo> parameters, MethodKind kind) {
        this(name, new ReturnInfo(rawReturnType, returnAnnotations), parameters, kind);
    }

    /** Backward-compatible — no return annotations. */
    public MethodInfo(String name, Class<?> rawReturnType,
                      List<ParameterInfo> parameters, MethodKind kind) {
        this(name, new ReturnInfo(rawReturnType, List.of()), parameters, kind);
    }

    public boolean hasParameters()    { return !parameters.isEmpty(); }
    public boolean hasReturnConstraints() { return returnType.isConstrained(); }
}