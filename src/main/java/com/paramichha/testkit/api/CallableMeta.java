package com.paramichha.testkit.api;

import java.util.List;

/**
 * Describes a constructor or method — anything callable on a class.
 * Constructor is kind = CONSTRUCTOR, returnType = void TypeInfo.
 *
 * Single model for both — the writer decides what to generate based on kind().
 */
public interface CallableMeta {

    /**
     * Method name, or the class simple name for constructors.
     * Use isConstructor() to distinguish.
     */
    String name();

    CallableKind kind();

    Visibility visibility();

    boolean isStatic();

    boolean isConstructor();

    /** Annotations declared on the method/constructor. */
    List<AnnotationInfo> annotations();

    /** Parameters in declaration order. */
    List<ParamMeta> parameters();

    /**
     * Return type.
     * CONSTRUCTOR / COMMAND → void TypeInfo.
     * Everything else → actual return type.
     */
    TypeInfo returnType();

    /** Checked exceptions in throws clause. */
    List<TypeInfo> declaredExceptions();

    boolean hasParameters();

    boolean isConstrained();

    boolean hasReturnConstraints();
}
