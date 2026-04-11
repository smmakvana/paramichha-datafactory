package com.paramichha.datafactory.builder;

/**
 * Represents one invalid value for a field that violates exactly one constraint.
 *
 * <p>Each instance pairs a field name with the constraint it violates and the
 * value that triggers the violation. {@code invalidList()} returns one
 * {@code ConstraintCase} per annotation on the field.
 *
 * @param fieldName        the name of the field this case belongs to
 * @param constraint       the short name of the violated constraint, e.g. {@code "NotBlank"}, {@code "Min"}
 * @param value            the invalid value — may be {@code null}
 * @param annotationDriven {@code true} if the violation is driven by a Jakarta annotation;
 *                         {@code false} if it is a type-default probe (e.g. null for an unannotated reference)
 */
public record ConstraintCase(
        String  fieldName,
        String  constraint,
        Object  value,
        boolean annotationDriven
) {

    /**
     * Creates an annotation-driven constraint case.
     *
     * @param fieldName  the field name
     * @param constraint the constraint name, e.g. {@code "Email"}, {@code "Min"}
     * @param value      the invalid value
     */
    public static ConstraintCase of(String fieldName, String constraint, Object value) {
        return new ConstraintCase(fieldName, constraint, value, true);
    }

    /**
     * Creates a type-default constraint case — not driven by an annotation,
     * but by the type's natural invalid boundary (e.g. {@code null} for any reference type).
     *
     * @param fieldName  the field name
     * @param constraint the constraint name
     * @param value      the invalid value
     */
    public static ConstraintCase typeDefault(String fieldName, String constraint, Object value) {
        return new ConstraintCase(fieldName, constraint, value, false);
    }
}
