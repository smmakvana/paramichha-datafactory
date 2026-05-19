package com.paramichha.testkit.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

public interface FieldInfo {
    boolean isConstrained();

    String name();

    Class<?> rawType();

    Type genericType();

    List<Annotation> validations();

    boolean required();

    boolean hasIndirectConstraints();
}
