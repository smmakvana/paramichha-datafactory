package com.paramichha.datafactory;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code Boolean} and {@code boolean} values.
 *
 * <p>Obtained via {@link DataFactory#bool()}.
 *
 * <pre>
 * boolean accepted = DataFactory.bool().assertTrue().valid();
 * boolean rejected = DataFactory.bool().assertFalse().valid();
 * boolean flag     = DataFactory.bool().valid();
 *
 * List&lt;Boolean&gt; bad = DataFactory.bool().assertTrue().invalidList();
 * </pre>
 */
public final class BoolFieldBuilder {

    private FieldBuilder<Boolean> delegate;

    BoolFieldBuilder(FieldBuilder<Boolean> delegate) {
        this.delegate = delegate;
    }

    /**
     * The generated value must be {@code true}.
     * Maps to {@code @AssertTrue}.
     */
    public BoolFieldBuilder assertTrue() {
        return with(AssertTrue.class);
    }

    /**
     * The generated value must be {@code false}.
     * Maps to {@code @AssertFalse}.
     */
    public BoolFieldBuilder assertFalse() {
        return with(AssertFalse.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public BoolFieldBuilder with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@AssertTrue"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public BoolFieldBuilder with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid {@code Boolean}.
     *
     * @return a valid boolean value satisfying all constraints
     * @throws DataFactoryException if no valid value can be generated
     */
    public Boolean valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values.
     *
     * @return list of valid boundary values
     */
    public List<Boolean> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Boolean}.
     *
     * @return a value that violates at least one constraint
     */
    public Boolean invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<Boolean> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<Boolean> stream(int count) {
        return delegate.stream(count);
    }
}
