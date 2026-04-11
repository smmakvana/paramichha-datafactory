package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Byte} / {@code byte} values. Obtained via {@link DataFactory#bytes()}. */
public interface ByteField extends FieldBuilder<Byte> {
    ByteField range(byte min, byte max);
    ByteField min(byte min);
    ByteField max(byte max);
    ByteField positive();
    ByteField negative();
    ByteField with(Class<? extends Annotation> annotation);
    ByteField with(String annotation);
    byte toByte();
}
