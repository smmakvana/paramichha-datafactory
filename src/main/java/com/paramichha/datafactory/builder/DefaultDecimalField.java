package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.*;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fluent builder for {@code BigDecimal} values.
 *
 * <p>Obtained via {@link DataFactory#decimal()}.
 *
 * <pre>
 * BigDecimal price  = DataFactory.decimal().positive().scale(2).valid();
 * BigDecimal gbp    = DataFactory.decimal().range("0.01", "999.99").scale(2).valid();
 * BigDecimal amount = DataFactory.decimal().digits(10, 2).valid();
 *
 * List&lt;BigDecimal&gt; prices = DataFactory.decimal().positive().scale(2).stream(100);
 * </pre>
 */
public final class DefaultDecimalField implements DecimalField {

    private FieldBuilder<BigDecimal> delegate;
    private int scale = -1;

    public DefaultDecimalField(FieldBuilder<BigDecimal> delegate) {
        this.delegate = delegate;
    }

    /**
     * Constrains the value to the given inclusive range.
     * Equivalent to {@code @DecimalMin(min) @DecimalMax(max)}.
     *
     * @param min inclusive minimum as a string, e.g. {@code "0.01"}
     * @param max inclusive maximum as a string, e.g. {@code "999.99"}
     */
    public DefaultDecimalField range(String min, String max) {
        return with("@DecimalMin(value=\"" + min + "\")")
              .with("@DecimalMax(value=\"" + max + "\")");
    }

    /**
     * Constrains the value to a minimum.
     * Equivalent to {@code @DecimalMin(min)}.
     *
     * @param min inclusive minimum as a string
     */
    public DefaultDecimalField min(String min) {
        return with("@DecimalMin(value=\"" + min + "\")");
    }

    /**
     * Constrains the value to a maximum.
     * Equivalent to {@code @DecimalMax(max)}.
     *
     * @param max inclusive maximum as a string
     */
    public DefaultDecimalField max(String max) {
        return with("@DecimalMax(value=\"" + max + "\")");
    }

    /**
     * The generated value must be strictly positive.
     * Equivalent to {@code @Positive}.
     */
    public DefaultDecimalField positive() {
        return with(Positive.class);
    }

    /**
     * The generated value must be strictly negative.
     * Equivalent to {@code @Negative}.
     */
    public DefaultDecimalField negative() {
        return with(Negative.class);
    }

    /**
     * Sets the number of decimal places applied to the returned value.
     * Applied via {@link BigDecimal#setScale(int, RoundingMode)} with {@code HALF_UP}.
     *
     * @param scale number of decimal places, e.g. {@code 2} for currency
     */
    public DefaultDecimalField scale(int scale) {
        this.scale = scale;
        return this;
    }

    /**
     * Constrains the value using {@code @Digits} — sets the maximum integer and
     * fraction digits, and records the fraction count as the scale for the returned value.
     * Equivalent to {@code @Digits(integer=integer, fraction=fraction)}.
     *
     * @param integer  maximum number of integer digits
     * @param fraction maximum number of fraction digits (also sets the scale)
     */
    public DefaultDecimalField digits(int integer, int fraction) {
        this.scale = fraction;
        return with("@Digits(integer=" + integer + ", fraction=" + fraction + ")");
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public DefaultDecimalField with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@DecimalMin(value=\"0.01\")"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public DefaultDecimalField with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid {@code BigDecimal}, scaled if {@link #scale(int)} was called.
     *
     * @return a valid BigDecimal
     * @throws DataFactoryException if no valid value can be generated
     */
    public BigDecimal valid() {
        return applyScale(delegate.valid());
    }

    /**
     * Returns one valid value as {@code BigDecimal}.
     * Alias for {@link #valid()}.
     *
     * @return a valid BigDecimal
     */
    public BigDecimal toBigDecimal() {
        return valid();
    }

    /**
     * Returns all boundary-covering valid values, each scaled if {@link #scale(int)} was called.
     *
     * @return list of valid boundary values
     */
    public List<BigDecimal> validList() {
        return delegate.validList().stream()
                .map(this::applyScale)
                .collect(Collectors.toList());
    }

    /**
     * Returns one invalid {@code BigDecimal}.
     *
     * @return a value that violates at least one constraint
     */
    public BigDecimal invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<BigDecimal> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values, each scaled if {@link #scale(int)} was called.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<BigDecimal> stream(int count) {
        return delegate.stream(count).stream()
                .map(this::applyScale)
                .collect(Collectors.toList());
    }

    private BigDecimal applyScale(BigDecimal value) {
        if (value == null || scale < 0) return value;
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
