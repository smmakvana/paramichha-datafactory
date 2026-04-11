package com.paramichha.datafactory;

import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Fluent builder for {@code Byte} and {@code byte} values.
 *
 * <p>Obtained via {@link DataFactory#bytes()}.
 *
 * <pre>
 * byte flags = DataFactory.bytes().positive().toByte();
 * byte n     = DataFactory.bytes().range((byte)1, (byte)100).toByte();
 * </pre>
 */
public final class ByteFieldBuilder {

    private FieldBuilder<Byte> delegate;

    ByteFieldBuilder(FieldBuilder<Byte> delegate) {
        this.delegate = delegate;
    }

    /**
     * Constrains the value to the given inclusive range.
     * Equivalent to {@code @Min(min) @Max(max)}.
     *
     * @param min inclusive minimum
     * @param max inclusive maximum
     */
    public ByteFieldBuilder range(byte min, byte max) {
        return with("@Min(" + min + ")").with("@Max(" + max + ")");
    }

    /**
     * Constrains the value to a minimum.
     * Equivalent to {@code @Min(min)}.
     *
     * @param min inclusive minimum
     */
    public ByteFieldBuilder min(byte min) {
        return with("@Min(" + min + ")");
    }

    /**
     * Constrains the value to a maximum.
     * Equivalent to {@code @Max(max)}.
     *
     * @param max inclusive maximum
     */
    public ByteFieldBuilder max(byte max) {
        return with("@Max(" + max + ")");
    }

    /**
     * The generated value must be strictly positive.
     * Equivalent to {@code @Positive}.
     */
    public ByteFieldBuilder positive() {
        return with(Positive.class);
    }

    /**
     * The generated value must be strictly negative.
     * Equivalent to {@code @Negative}.
     */
    public ByteFieldBuilder negative() {
        return with(Negative.class);
    }

    /**
     * Adds a constraint annotation by class reference.
     *
     * @param annotation the annotation type to add
     */
    public ByteFieldBuilder with(Class<? extends Annotation> annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Adds a constraint annotation by string, e.g. {@code "@Min(1)"}.
     *
     * @param annotation the annotation string
     * @throws AnnotationParseException if the string cannot be parsed
     */
    public ByteFieldBuilder with(String annotation) {
        delegate = delegate.with(annotation);
        return this;
    }

    /**
     * Returns one valid value as a primitive {@code byte}.
     *
     * @return a valid byte
     * @throws DataFactoryException if no valid value can be generated
     */
    public byte toByte() {
        return delegate.valid();
    }

    /**
     * Returns one valid {@code Byte}.
     *
     * @return a valid Byte
     * @throws DataFactoryException if no valid value can be generated
     */
    public Byte valid() {
        return delegate.valid();
    }

    /**
     * Returns all boundary-covering valid values.
     *
     * @return list of valid boundary values
     */
    public List<Byte> validList() {
        return delegate.validList();
    }

    /**
     * Returns one invalid {@code Byte}.
     *
     * @return a value that violates at least one constraint
     */
    public Byte invalid() {
        return delegate.invalid();
    }

    /**
     * Returns all invalid values, one per constraint.
     *
     * @return list of invalid values
     */
    public List<Byte> invalidList() {
        return delegate.invalidList();
    }

    /**
     * Returns {@code count} valid values.
     *
     * @param count the number of values to generate
     * @return list of valid values
     */
    public List<Byte> stream(int count) {
        return delegate.stream(count);
    }
}
