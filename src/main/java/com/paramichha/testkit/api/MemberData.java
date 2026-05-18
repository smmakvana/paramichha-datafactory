package com.paramichha.testkit.api;

import java.util.List;

/**
 * A field paired with its DataFactory-generated values.
 * The writer reads this — never calls DataFactory directly.
 */
public interface MemberData {

    MemberMeta meta();

    /**
     * Valid values — at least one, possibly several boundary variants.
     * DataFactory.field(name, type, validations).validList()
     */
    List<Object> validValues();

    /**
     * Invalid values — empty if field has no constraints.
     * DataFactory.field(name, type, validations).invalidList()
     */
    List<Object> invalidValues();

    boolean isConstrained();
}
