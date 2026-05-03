package com.paramichha.testkit;

import com.paramichha.testkit.model.ClassMeta;

/**
 * Reads a {@link Class} and produces {@link ClassMeta}.
 * Implement to support non-standard class structures.
 */
public interface ClassReader {
    ClassMeta read(Class<?> type);
}
