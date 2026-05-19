package com.paramichha.testkit;

import com.paramichha.testkit.model.ClassInfo;

/**
 * Reads a {@link Class} and produces {@link ClassInfo}.
 * Implement to support non-standard class structures.
 */
public interface ClassReader {
    ClassInfo read(Class<?> type);
}
