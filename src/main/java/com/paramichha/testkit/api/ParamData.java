package com.paramichha.testkit.api;

import java.util.List;

/**
 * A parameter paired with its DataFactory-generated values.
 * The writer reads this — never calls DataFactory directly.
 */
public interface ParamData {

    ParamMeta meta();

    /** Valid value — DataFactory.field(name, type, validations).valid(). */
    Object validValue();

    /**
     * Invalid values — DataFactory.field(name, type, validations).invalidList().
     * Empty if param has no constraints.
     */
    List<Object> invalidValues();

    boolean hasInvalidValues();
}
