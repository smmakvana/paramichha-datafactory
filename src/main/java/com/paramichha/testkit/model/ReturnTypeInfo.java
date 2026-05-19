package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

public interface ReturnTypeInfo {
    boolean isVoid();

    boolean isConstrained();

    boolean isPrimitive();

    Class<?> type();

    List<Annotation> annotations();
}
