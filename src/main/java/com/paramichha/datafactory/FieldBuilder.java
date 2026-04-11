package com.paramichha.datafactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for generating valid and invalid values for a single field.
 *
 * <p>Obtained via {@link DataFactory#field(Class)}, {@link DataFactory#field(java.lang.reflect.Field)},
 * or the typed convenience builders such as {@link DataFactory#string()},
 * {@link DataFactory#integer()}, {@link DataFactory#date()}, etc.
 *
 * <h2>Common patterns</h2>
 * <pre>
 * // Satisfy constraints
 * String email = DataFactory.field(String.class)
 *                           .with(Email.class)
 *                           .with("@Size(max=100)")
 *                           .valid();
 *
 * // One invalid value
 * String bad = DataFactory.string().email().invalid();
 *
 * // All invalid values — one per constraint
 * List&lt;String&gt; bad = DataFactory.string().email().invalidList();
 * // → [null, "", "notanemail", "toolong..."]
 *
 * // All boundary-covering valid values
 * List&lt;Integer&gt; ages = DataFactory.integer().range(18, 120).validList();
 * // → [18, 69, 120, 34]  (min, midpoint, max, semantic)
 *
 * // Bulk generation
 * List&lt;String&gt; emails = DataFactory.string().email().stream(100);
 * </pre>
 *
 * @param <T> the field value type
 * @see DataFactory#string()
 * @see DataFactory#integer()
 * @see DataFactory#decimal()
 * @see DataFactory#date()
 */
public interface FieldBuilder<T> {

    /**
     * Adds a constraint annotation to this field by class reference.
     * The annotation must have no required attributes — use the string form for
     * annotations that require values such as {@code @Min} or {@code @Size}.
     * <pre>
     * DataFactory.field(String.class).with(NotBlank.class).with(Email.class).valid()
     * </pre>
     */
    FieldBuilder<T> with(Class<? extends Annotation> annotation);

    /**
     * Adds a constraint annotation to this field by string.
     * The string must start with {@code @} and match Jakarta annotation syntax.
     * <pre>
     * DataFactory.field(Integer.class).with("@Min(18)").with("@Max(120)").valid()
     * DataFactory.field(String.class).with("@Size(min=2, max=50)").valid()
     * </pre>
     *
     * @throws AnnotationParseException if the string cannot be parsed as an annotation
     */
    FieldBuilder<T> with(String annotation);

    /**
     * Returns one value satisfying all constraints added so far.
     * The value is semantically realistic — not just the minimum valid value.
     *
     * @throws DataFactoryException if no valid value can be generated
     */
    T valid();

    /**
     * Returns all boundary-covering valid values.
     * For a numeric field with {@code @Min(18) @Max(120)}, this returns values
     * at the minimum, a midpoint, the maximum, and a semantically realistic value —
     * for example {@code [18, 69, 120, 34]}.
     *
     * <p>In DEV mode with unannotated numeric fields, this covers the full type
     * range: {@code [Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 42]}.
     */
    List<T> validList();

    /**
     * Returns one value that violates at least one constraint.
     * If multiple constraints are present, the value violates the first one
     * that can produce an invalid value — typically a null or out-of-range value.
     */
    T invalid();

    /**
     * Returns all invalid values, one per constraint.
     * Each value in the list violates exactly one constraint.
     *
     * <p>For a field annotated {@code @NotBlank @Email @Size(max=50)},
     * this returns four values: null (NotNull), blank string (NotBlank),
     * a non-email string (Email), and a string over 50 characters (Size).
     */
    List<T> invalidList();

    /**
     * Returns {@code count} valid values.
     * Values are independently generated — each call to the underlying generator
     * is a fresh random draw, so results vary.
     * <pre>
     * List&lt;String&gt; emails = DataFactory.string().email().stream(100);
     * List&lt;Integer&gt; ages   = DataFactory.integer().range(18, 65).stream(1000);
     * </pre>
     */
    List<T> stream(int count);
}
