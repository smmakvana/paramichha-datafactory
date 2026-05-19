package com.paramichha.testkit.model;

import java.util.List;

public interface ClassInfo {
    String simpleName();

    String packageName();

    ConstructorStyle constructorStyle();

    List<FieldInfo> fields();

    List<MethodInfo> methods();

    boolean utilityClass();

    Class<?> type();
}
