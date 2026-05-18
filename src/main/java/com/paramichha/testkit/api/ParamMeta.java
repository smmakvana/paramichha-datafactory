package com.paramichha.testkit.api;

import java.util.List;

/**
 * Describes a single parameter of a constructor or method.
 * Everything DataFactory needs to generate valid and invalid values.
 */
public interface ParamMeta {

    String name();

    TypeInfo typeInfo();

    /** Jakarta validation annotations — drives DataFactory value generation. */
    List<AnnotationInfo> validations();

    boolean isVarArgs();

    boolean isConstrained();
}
