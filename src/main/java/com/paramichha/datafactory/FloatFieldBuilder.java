package com.paramichha.datafactory;

import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code Float} and {@code float} values.
 *
 * <p>Obtained via {@link DataFactory#floats()}.
 *
 * <pre>
 * float rate  = DataFactory.floats().positive().toFloat();
 * float ratio = DataFactory.floats().valid();
 * </pre>
 *
 * <p>Note: Jakarta's {@code @Min} and {@code @Max} do not support {@code float}.
 * Use {@link DecimalFieldBuilder} via {@link DataFactory#decimal()} for
 * precise decimal range constraints.
 */
public final class FloatFieldBuilder {

    private FieldBuilder<Float> delegate;

    FloatFieldBuilder(FieldBuilder<Float> delegate) {
        this.delegate = delegate;
    }

    /**
     * The generated value must be strictly positive.
     * Equivalent to {@code @Positive}.
     */
    public FloatFieldBuilder positive() {
        return with(Positive.class);
    }

    /**
     * The generated value must be strictly negative.
     * Equivalent to {@code @Negative}.
     */
    public FloatFieldBuilder negative() {
        return with(Negative.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public FloatFieldBuilder with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Positive"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public FloatFieldBuilder with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid value as a primitive {@code float}.
     *
     * @return a valid float
     * @throws DataFactoryException if no valid value can be generated
     */
    public float toFloat() {
        return delegate.valid();
    }

    /**
     * Returns one valid {@code Float}.
     *
     * @return a valid Float
     * @throws DataFactoryException if no valid value can be generated
     */
    public Float valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values.
     *
     * @return list of valid boundary values
     */
    public List<Float> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Float}.
     *
     * @return a value that violates at least one constraint
     */
    public Float invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<Float> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<Float> stream(int count) {
        return delegate.stream(count);
    }
}
