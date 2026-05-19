package com.paramichha.testkit.model;

import java.util.List;

public interface ClassMetaData {
    String simpleName();

    String packageName();

    ConstructorStyle constructorStyle();

    List<FieldInfo> fields();

    List<MethodInfo> methods();

    boolean utilityClass();

    Class<?> type();

    public enum ConstructorStyle {LOMBOK_BUILDER, RECORD, ALL_ARGS, SETTER}
}
