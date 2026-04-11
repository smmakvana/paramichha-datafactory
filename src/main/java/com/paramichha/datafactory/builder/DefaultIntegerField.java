package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code Integer} and {@code int} values.
 *
 * <p>Obtained via {@link DataFactory#integer()}.
 *
 * <pre>
 * int age   = DataFactory.integer().range(18, 65).valid();
 * int score = DataFactory.integer().positive().valid();
 *
 * List&lt;Integer&gt; ages = DataFactory.integer().range(18, 90).stream(1000);
 * List&lt;Integer&gt; bad  = DataFactory.integer().range(18, 65).invalidList();
 * </pre>
 */
public final class DefaultIntegerField implements IntegerField {

    private FieldBuilder<Integer> delegate;

    public DefaultIntegerField(FieldBuilder<Integer> delegate) {
        this.delegate = delegate;
    }

    /**
     * Constrains the value to the given inclusive range.
     * Equivalent to {@code @Min(min) @Max(max)}.
     *
     * @param min inclusive minimum
     * @param max inclusive maximum
     */
    public DefaultIntegerField range(int min, int max) {
        return with("@Min(" + min + ")").with("@Max(" + max + ")");
    }

    /**
     * Constrains the value to a minimum.
     * Equivalent to {@code @Min(min)}.
     *
     * @param min inclusive minimum
     */
    public DefaultIntegerField min(int min) {
        return with("@Min(" + min + ")");
    }

    /**
     * Constrains the value to a maximum.
     * Equivalent to {@code @Max(max)}.
     *
     * @param max inclusive maximum
     */
    public DefaultIntegerField max(int max) {
        return with("@Max(" + max + ")");
    }

    /**
     * The generated value must be strictly positive.
     * Equivalent to {@code @Positive}.
     */
    public DefaultIntegerField positive() {
        return with(Positive.class);
    }

    /**
     * The generated value must be strictly negative.
     * Equivalent to {@code @Negative}.
     */
    public DefaultIntegerField negative() {
        return with(Negative.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public DefaultIntegerField with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Min(18)"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public DefaultIntegerField with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid value as a primitive {@code int}.
     *
     * @return a valid int
     * @throws DataFactoryException if no valid value can be generated
     */
    public int toInt() {
        return delegate.valid();
    }

    /**
     * Returns one valid {@code Integer}.
     *
     * @return a valid Integer
     * @throws DataFactoryException if no valid value can be generated
     */
    public Integer valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values — minimum, midpoint, maximum, semantic.
     *
     * @return list of valid boundary values
     */
    public List<Integer> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Integer}.
     *
     * @return a value that violates at least one constraint
     */
    public Integer invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<Integer> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<Integer> stream(int count) {
        return delegate.stream(count);
    }
}
