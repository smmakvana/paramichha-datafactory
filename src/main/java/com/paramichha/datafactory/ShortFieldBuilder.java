package com.paramichha.datafactory;

import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code Short} and {@code short} values.
 *
 * <p>Obtained via {@link DataFactory#shorts()}.
 *
 * <pre>
 * short flags = DataFactory.shorts().positive().toShort();
 * short n     = DataFactory.shorts().range(1, 100).toShort();
 * </pre>
 */
public final class ShortFieldBuilder {

    private FieldBuilder<Short> delegate;

    ShortFieldBuilder(FieldBuilder<Short> delegate) {
        this.delegate = delegate;
    }

    /**
     * Constrains the value to the given inclusive range.
     * Equivalent to {@code @Min(min) @Max(max)}.
     *
     * @param min inclusive minimum
     * @param max inclusive maximum
     */
    public ShortFieldBuilder range(short min, short max) {
        return with("@Min(" + min + ")").with("@Max(" + max + ")");
    }

    /**
     * Constrains the value to a minimum.
     * Equivalent to {@code @Min(min)}.
     *
     * @param min inclusive minimum
     */
    public ShortFieldBuilder min(short min) {
        return with("@Min(" + min + ")");
    }

    /**
     * Constrains the value to a maximum.
     * Equivalent to {@code @Max(max)}.
     *
     * @param max inclusive maximum
     */
    public ShortFieldBuilder max(short max) {
        return with("@Max(" + max + ")");
    }

    /**
     * The generated value must be strictly positive.
     * Equivalent to {@code @Positive}.
     */
    public ShortFieldBuilder positive() {
        return with(Positive.class);
    }

    /**
     * The generated value must be strictly negative.
     * Equivalent to {@code @Negative}.
     */
    public ShortFieldBuilder negative() {
        return with(Negative.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public ShortFieldBuilder with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Min(1)"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public ShortFieldBuilder with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid value as a primitive {@code short}.
     *
     * @return a valid short
     * @throws DataFactoryException if no valid value can be generated
     */
    public short toShort() {
        return delegate.valid();
    }

    /**
     * Returns one valid {@code Short}.
     *
     * @return a valid Short
     * @throws DataFactoryException if no valid value can be generated
     */
    public Short valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values.
     *
     * @return list of valid boundary values
     */
    public List<Short> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Short}.
     *
     * @return a value that violates at least one constraint
     */
    public Short invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<Short> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<Short> stream(int count) {
        return delegate.stream(count);
    }
}
