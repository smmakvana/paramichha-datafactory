package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fluent builder for {@code LocalDateTime} values.
 *
 * <p>Obtained via {@link DataFactory#dateTime()}.
 *
 * <pre>
 * LocalDateTime created  = DataFactory.dateTime().past().valid();
 * LocalDateTime deadline = DataFactory.dateTime().future().valid();
 *
 * List&lt;LocalDateTime&gt; times = DataFactory.dateTime().past().stream(10);
 * </pre>
 */
public final class DefaultDateTimeField implements DateTimeField {

    private FieldBuilder<LocalDateTime> delegate;

    public DefaultDateTimeField(FieldBuilder<LocalDateTime> delegate) {
        this.delegate = delegate;
    }

    /**
     * The generated datetime must be strictly in the past.
     * Equivalent to {@code @Past}.
     */
    public DefaultDateTimeField past() {
        return with(Past.class);
    }

    /**
     * The generated datetime must be in the past or now.
     * Equivalent to {@code @PastOrPresent}.
     */
    public DefaultDateTimeField pastOrPresent() {
        return with(PastOrPresent.class);
    }

    /**
     * The generated datetime must be strictly in the future.
     * Equivalent to {@code @Future}.
     */
    public DefaultDateTimeField future() {
        return with(Future.class);
    }

    /**
     * The generated datetime must be in the future or now.
     * Equivalent to {@code @FutureOrPresent}.
     */
    public DefaultDateTimeField futureOrPresent() {
        return with(FutureOrPresent.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public DefaultDateTimeField with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Future"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public DefaultDateTimeField with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid {@code LocalDateTime}.
     *
     * @return a valid datetime satisfying all constraints
     * @throws DataFactoryException if no valid value can be generated
     */
    public LocalDateTime valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid datetimes.
     *
     * @return list of valid boundary datetimes
     */
    public List<LocalDateTime> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code LocalDateTime}.
     *
     * @return a datetime that violates at least one constraint
     */
    public LocalDateTime invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid datetimes, one per constraint.
     *
     * @return list of invalid datetimes
     */
    public List<LocalDateTime> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid datetimes.
     *
     * @param count the number of values to generate
     * @return list of valid datetimes
     */
    public List<LocalDateTime> stream(int count) {
        return delegate.stream(count);
    }
}
