package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.util.List;

public interface ParameterInfo {
    boolean isConstrained();

    String name();

    Class<?> type();

    List<Annotation> validations();

    boolean hasIndirectConstraints();
}
