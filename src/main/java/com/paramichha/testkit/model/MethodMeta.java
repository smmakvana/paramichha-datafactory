package com.paramichha.testkit.model;

import java.util.List;

/**
 * Metadata for one public method — name, return type, parameters, kind.
 * Produced by {@link com.paramichha.testkit.reader.DefaultClassReader}.
 */
public record MethodMeta(
        String name,
        Class<?> returnType,
        List<ParameterMeta> parameters,
        MethodKind kind
) {
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public boolean returnsVoid() {
        return returnType == void.class || returnType == Void.class;
    }

    public boolean returnsPrimitive() {
        return returnType.isPrimitive() && returnType != void.class;
    }

    public boolean returnsObject() {
        return !returnsVoid() && !returnsPrimitive();
    }
}
