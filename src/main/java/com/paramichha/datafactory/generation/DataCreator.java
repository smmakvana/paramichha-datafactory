package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

/**
 * Produces a realistic value for one field at one boundary target.
 * Stateless — {@link DataMachine} carries all randomness and seed state.
 */
interface DataCreator {
    boolean supports(Class<?> fieldType);
    Object shape(FieldConstraints field, BoundaryTarget target, DataMachine machine);
}
