package com.paramichha.testkit.model;

import java.util.List;

public interface MethodInfo {
    boolean hasParameters();

    boolean hasReturnConstraints();

    String name();

    ReflectionReturnTypeInfo returnType();

    List<ReflectionParameterInfo> parameters();

    MethodKind kind();
}
