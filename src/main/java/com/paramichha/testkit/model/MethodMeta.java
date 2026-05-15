package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

public record MethodMeta(
        String name,
        ReturnTypeMeta returnType,
        List<ParameterMeta> parameters,
        MethodKind kind
) {
    /** Backward-compatible — wraps raw type + annotations into ReturnTypeMeta. */
    public MethodMeta(String name, Class<?> rawReturnType,
                      List<Annotation> returnAnnotations,
                      List<ParameterMeta> parameters, MethodKind kind) {
        this(name, new ReturnTypeMeta(rawReturnType, returnAnnotations), parameters, kind);
    }

    /** Backward-compatible — no return annotations. */
    public MethodMeta(String name, Class<?> rawReturnType,
                      List<ParameterMeta> parameters, MethodKind kind) {
        this(name, new ReturnTypeMeta(rawReturnType, List.of()), parameters, kind);
    }

    public boolean hasParameters()    { return !parameters.isEmpty(); }
    public boolean hasReturnConstraints() { return returnType.isConstrained(); }
}