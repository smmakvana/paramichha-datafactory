package com.paramichha.testkit.model;

import com.paramichha.datafactory.GenerationMode;
import java.util.List;
import java.util.Map;

/**
 * DataFactory output captured at generation time.
 * If {@link #error()} is non-null, DataFactory failed and the generator
 * will write a commented placeholder file instead.
 */
public record TestFixture(
        Class<?> type,
        Object base,
        GenerationMode mode,
        List<?> violations,
        Map<String, List<?>> validAllPerField,
        List<?> validObjects,
        String error
) {
    public boolean hasError() { return error != null; }

    /** True if any field carries a Jakarta validation annotation. */
    public boolean hasJakarta(ClassInfo meta) {
        return meta.fields().stream().anyMatch(f -> !f.validations().isEmpty());
    }
}
