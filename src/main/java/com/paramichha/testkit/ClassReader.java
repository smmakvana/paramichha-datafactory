package com.paramichha.testkit;

import com.paramichha.testkit.model.ClassInfo;
import com.paramichha.testkit.model.ReflectionClassInfo;

/**
 * Reads a {@link Class} and produces {@link ReflectionClassInfo}.
 * Implement to support non-standard class structures.
 */
public interface ClassReader {
    ClassInfo read(Class<?> type);
}
