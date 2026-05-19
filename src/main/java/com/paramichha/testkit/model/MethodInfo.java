package com.paramichha.testkit.model;

import java.util.List;

public interface MethodInfo {
    boolean hasParameters();

    boolean hasReturnConstraints();

    String name();

    ReturnTypeInfo returnType();

    List<ParameterInfo> parameters();

    MethodKind kind();
}
