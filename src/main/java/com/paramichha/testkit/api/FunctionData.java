package com.paramichha.testkit.api;

import java.util.List;

/**
 * A constructor or method paired with DataFactory-generated values per parameter.
 * The writer reads this — never calls DataFactory directly.
 */
public interface FunctionData {

    CallableMeta meta();

    /** One ParamData per parameter — in declaration order. */
    List<ParamData> params();

    /** True if any param has invalid values — drives whether reject test is generated. */
    boolean hasInvalidInputs();

    boolean isConstrained();
}
