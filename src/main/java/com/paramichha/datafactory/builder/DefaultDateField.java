package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.List;

/**
 * Fluent builder for {@code LocalDate} values.
 *
 * <p>Obtained via {@link DataFactory#date()}.
 *
 * <pre>
 * LocalDate dob    = DataFactory.date().past().valid();
 * LocalDate expiry = DataFactory.date().future().valid();
 *
 * List&lt;LocalDate&gt; dates = DataFactory.date().past().stream(10);
 * </pre>
 */
public final class DefaultDateField implements DateField {

    private FieldBuilder<LocalDate> delegate;

    public DefaultDateField(FieldBuilder<LocalDate> delegate) {
        this.delegate = delegate;
    }

    /**
     * The generated date must be strictly in the past.
     * Equivalent to {@code @Past}.
     */
    public DefaultDateField past() {
        return with(Past.class);
    }

    /**
     * The generated date must be in the past or today.
     * Equivalent to {@code @PastOrPresent}.
     */
    public DefaultDateField pastOrPresent() {
        return with(PastOrPresent.class);
    }

    /**
     * The generated date must be strictly in the future.
     * Equivalent to {@code @Future}.
     */
    public DefaultDateField future() {
        return with(Future.class);
    }

    /**
     * The generated date must be in the future or today.
     * Equivalent to {@code @FutureOrPresent}.
     */
    public DefaultDateField futureOrPresent() {
        return with(FutureOrPresent.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public DefaultDateField with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Past"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public DefaultDateField with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid {@code LocalDate}.
     *
     * @return a valid date satisfying all constraints
     * @throws DataFactoryException if no valid value can be generated
     */
    public LocalDate valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid dates.
     *
     * @return list of valid boundary dates
     */
    public List<LocalDate> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code LocalDate}.
     *
     * @return a date that violates at least one constraint
     */
    public LocalDate invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid dates, one per constraint.
     *
     * @return list of invalid dates
     */
    public List<LocalDate> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid dates.
     *
     * @param count the number of values to generate
     * @return list of valid dates
     */
    public List<LocalDate> stream(int count) {
        return delegate.stream(count);
    }
}
