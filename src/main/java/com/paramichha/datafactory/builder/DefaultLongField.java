package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code Long} and {@code long} values.
 *
 * <p>Obtained via {@link DataFactory#longVal()}.
 *
 * <pre>
 * long id    = DataFactory.longVal().positive().toLong();
 * long price = DataFactory.longVal().range(1L, 9999L).toLong();
 *
 * List&lt;Long&gt; ids = DataFactory.longVal().positive().stream(500);
 * </pre>
 */
public final class DefaultLongField implements LongField {

    private FieldBuilder<Long> delegate;

    public DefaultLongField(FieldBuilder<Long> delegate) {
        this.delegate = delegate;
    }

    /**
     * Constrains the value to the given inclusive range.
     * Equivalent to {@code @Min(min) @Max(max)}.
     *
     * @param min inclusive minimum
     * @param max inclusive maximum
     */
    public DefaultLongField range(long min, long max) {
        return with("@Min(" + min + ")").with("@Max(" + max + ")");
    }

    /**
     * Constrains the value to a minimum.
     * Equivalent to {@code @Min(min)}.
     *
     * @param min inclusive minimum
     */
    public DefaultLongField min(long min) {
        return with("@Min(" + min + ")");
    }

    /**
     * Constrains the value to a maximum.
     * Equivalent to {@code @Max(max)}.
     *
     * @param max inclusive maximum
     */
    public DefaultLongField max(long max) {
        return with("@Max(" + max + ")");
    }

    /**
     * The generated value must be strictly positive.
     * Equivalent to {@code @Positive}.
     */
    public DefaultLongField positive() {
        return with(Positive.class);
    }

    /**
     * The generated value must be strictly negative.
     * Equivalent to {@code @Negative}.
     */
    public DefaultLongField negative() {
        return with(Negative.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public DefaultLongField with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Min(100)"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public DefaultLongField with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid value as a primitive {@code long}.
     *
     * @return a valid long
     * @throws DataFactoryException if no valid value can be generated
     */
    public long toLong() {
        return delegate.valid();
    }

    /**
     * Returns one valid {@code Long}.
     *
     * @return a valid Long
     * @throws DataFactoryException if no valid value can be generated
     */
    public Long valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values — minimum, midpoint, maximum, semantic.
     *
     * @return list of valid boundary values
     */
    public List<Long> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Long}.
     *
     * @return a value that violates at least one constraint
     */
    public Long invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<Long> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<Long> stream(int count) {
        return delegate.stream(count);
    }
}
