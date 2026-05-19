package com.paramichha.testkit.model;

import java.util.List;

public interface ClassInfo {
    String simpleName();

    String packageName();

    ReflectionClassInfo.ConstructorStyle constructorStyle();

    List<ReflectionFieldInfo> fields();

    List<ReflectionMethodInfo> methods();

    boolean utilityClass();

    Class<?> type();
}
