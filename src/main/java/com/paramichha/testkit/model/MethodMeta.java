package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Metadata for one public method — name, return type, parameters, kind.
 * returnAnnotations captures Jakarta constraints declared on the return type.
 */
public record MethodMeta(
        String name,
        Class<?> returnType,
        List<Annotation> returnAnnotations,
        List<ParameterMeta> parameters,
        MethodKind kind
) {
    /** Backward-compatible — no return annotations. */
    public MethodMeta(String name, Class<?> returnType,
                      List<ParameterMeta> parameters, MethodKind kind) {
        this(name, returnType, List.of(), parameters, kind);
    }

    public boolean hasParameters()    { return !parameters.isEmpty(); }
    public boolean returnsVoid()      { return returnType == void.class || returnType == Void.class; }
    public boolean returnsPrimitive() { return returnType.isPrimitive() && returnType != void.class; }
    public boolean returnsObject()    { return !returnsVoid() && !returnsPrimitive(); }
    public boolean hasReturnConstraints() { return !returnAnnotations.isEmpty(); }
}