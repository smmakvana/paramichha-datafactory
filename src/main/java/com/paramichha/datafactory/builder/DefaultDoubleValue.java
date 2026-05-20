package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code Double} and {@code double} values.
 *
 * <p>Obtained via {@link DataFactory#doubles()}.
 *
 * <pre>
 * double price  = DataFactory.doubles().positive().toDouble();
 * double ratio  = DataFactory.doubles().valid();
 * </pre>
 *
 * <p>Note: Jakarta's {@code @Min} and {@code @Max} do not support {@code double}.
 * Use {@link BigDecimalValue} via {@link DataFactory#decimal()} for
 * precise decimal range constraints.
 */
public final class DefaultDoubleValue implements DoubleValue {

    private FieldValue<Double> delegate;

    public DefaultDoubleValue(FieldValue<Double> delegate) {
        this.delegate = delegate;
    }

    /**
     * The generated value must be strictly positive.
     * Equivalent to {@code @Positive}.
     */
    public DefaultDoubleValue positive() {
        return with(Positive.class);
    }

    /**
     * The generated value must be strictly negative.
     * Equivalent to {@code @Negative}.
     */
    public DefaultDoubleValue negative() {
        return with(Negative.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public DefaultDoubleValue with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Positive"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public DefaultDoubleValue with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid value as a primitive {@code double}.
     *
     * @return a valid double
     * @throws DataFactoryException if no valid value can be generated
     */
    public double toDouble() {
        return delegate.valid();
    }

    /**
     * Returns one valid {@code Double}.
     *
     * @return a valid Double
     * @throws DataFactoryException if no valid value can be generated
     */
    @Override
    public FieldValue<Double> seed(long seed) {
        delegate = delegate.seed(seed);
        return this;
    }

    public Double valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values.
     *
     * @return list of valid boundary values
     */
    public List<Double> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Double}.
     *
     * @return a value that violates at least one constraint
     */
    public Double invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<Double> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<Double> stream(int count) {
        return delegate.stream(count);
    }
}
