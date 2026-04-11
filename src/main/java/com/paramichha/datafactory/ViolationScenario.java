package com.paramichha.datafactory;

/**
 * One test scenario — a fully-valid object except for one field that violates one constraint.
 *
 * @param fieldName      the field that holds an invalid value
 * @param constraint     the constraint violated, e.g. "NotBlank", "Min"
 * @param testNameSuffix suggested name fragment for test methods, e.g. "email_null"
 * @param invalidValue   the invalid value placed in the field
 * @param object         the fully-constructed object with one field invalid
 */
public record ViolationScenario<T>(
        String fieldName,
        String constraint,
        String testNameSuffix,
        Object invalidValue,
        T object
) {
}
