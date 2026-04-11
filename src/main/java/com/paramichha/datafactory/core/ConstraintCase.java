package com.paramichha.datafactory.core;

/**
 * One constraint case for a field — the invalid value plus metadata needed to name and assert a test.
 *
 * @param fieldName      the field name
 * @param constraint     the constraint, e.g. "NotBlank", "Min"
 * @param testNameSuffix suggested name fragment, e.g. "email_null"
 * @param value          the invalid value
 * @param sourceCode     the value as a Java source literal, e.g. {@code "null"}, {@code "17"}
 * @param typeDefault    {@code true} if type-level default; {@code false} if annotation-driven
 */
public record ConstraintCase(
        String fieldName,
        String constraint,
        String testNameSuffix,
        Object value,
        String sourceCode,
        boolean typeDefault
) {

    /**
     * Creates an annotation-driven case guaranteed to produce a Jakarta violation.
     */
    public static ConstraintCase of(String fieldName, String constraint,
                                    String suffix, Object value, String sourceCode) {
        return new ConstraintCase(fieldName, constraint, suffix, value, sourceCode, false);
    }

    /**
     * Creates a type-level default case that may not trigger Jakarta validation.
     */
    public static ConstraintCase typeDefault(String fieldName, String constraint,
                                             String suffix, Object value, String sourceCode) {
        return new ConstraintCase(fieldName, constraint, suffix, value, sourceCode, true);
    }

    /**
     * Returns {@code true} when driven by a Jakarta annotation.
     */
    public boolean isAnnotationDriven() {
        return !typeDefault;
    }

    /**
     * Returns a suggested test method name, e.g. {@code "email_null_shouldFailValidation"}.
     */
    public String testMethodName() {
        return testNameSuffix + "_shouldFailValidation";
    }
}
