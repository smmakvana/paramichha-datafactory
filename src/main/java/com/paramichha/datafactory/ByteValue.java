package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Byte} / {@code byte} values. Obtained via {@link DataFactory#bytes()}. */
public interface ByteValue extends FieldValue<Byte> {
    ByteValue range(byte min, byte max);
    ByteValue min(byte min);
    ByteValue max(byte max);
    ByteValue positive();
    ByteValue negative();
    ByteValue with(Class<? extends Annotation> annotation);
    ByteValue with(String annotation);
    byte toByte();
}
