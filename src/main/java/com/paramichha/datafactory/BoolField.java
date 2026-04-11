package com.paramichha.datafactory;

import java.lang.annotation.Annotation;

/** Fluent builder for {@code Boolean} / {@code boolean} values. Obtained via {@link DataFactory#bool()}. */
public interface BoolField extends FieldBuilder<Boolean> {
    BoolField assertTrue();
    BoolField assertFalse();
    BoolField with(Class<? extends Annotation> annotation);
    BoolField with(String annotation);
}
