package com.paramichha.datafactory;

import java.util.List;

/**
 * Valid and invalid values for one field, generated without building the whole object.
 *
 * <p>Returned by {@link DataBuilder#fields()}. Each field is resolved on its own,
 * so a field DataFactory cannot handle does not prevent the others from being generated.
 *
 * @param fieldName    the field name
 * @param fieldType    the declared field type
 * @param validValues  values that satisfy every constraint on the field — boundary
 *                     values for scalars, one representative value for containers
 *                     and nested objects
 * @param invalidCases one case per constraint on the field, each violating exactly that
 *                     constraint; type-default probes when the field has no annotations
 */
public record FieldCases(
        String fieldName, Class<?> fieldType, List<Object> validValues, List<ConstraintCase> invalidCases) {
    public FieldCases {
        validValues = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(validValues));
        invalidCases = List.copyOf(invalidCases);
    }
}
