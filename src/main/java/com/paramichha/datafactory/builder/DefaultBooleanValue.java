package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.AnnotationParseException;
import com.paramichha.datafactory.BooleanValue;
import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.DataFactoryException;
import com.paramichha.datafactory.FieldValue;
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
public final class DefaultBooleanValue implements BooleanValue {

    private FieldValue<Boolean> delegate;

    public DefaultBooleanValue(FieldValue<Boolean> delegate) {
        this.delegate = delegate;
    }

    /**
     * The generated value must be {@code true}.
     * Maps to {@code @AssertTrue}.
     */
    @Override
    public DefaultBooleanValue assertTrue() {
        return with(AssertTrue.class);
    }

    /**
     * The generated value must be {@code false}.
     * Maps to {@code @AssertFalse}.
     */
    @Override
    public DefaultBooleanValue assertFalse() {
        return with(AssertFalse.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    @Override
    public DefaultBooleanValue with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@AssertTrue"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    @Override
    public DefaultBooleanValue with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid {@code Boolean}.
     *
     * @return a valid boolean value satisfying all constraints
     * @throws DataFactoryException if no valid value can be generated
     */
    @Override
    public FieldValue<Boolean> seed(long seed) {
        delegate = delegate.seed(seed);
        return this;
    }

    @Override
    public Boolean valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values.
     *
     * @return list of valid boundary values
     */
    @Override
    public List<Boolean> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Boolean}.
     *
     * @return a value that violates at least one constraint
     */
    @Override
    public Boolean invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    @Override
    public List<Boolean> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    @Override
    public List<Boolean> stream(int count) {
        return delegate.stream(count);
    }
}
