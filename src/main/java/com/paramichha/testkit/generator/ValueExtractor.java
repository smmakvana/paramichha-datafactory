package com.paramichha.testkit.generator;

import com.paramichha.testkit.model.FieldInfo;

import java.util.List;

/**
 * Extracts field values from an object instance.
 * Used to decompose DataFactory output into per-field literals.
 */
public interface ValueExtractor {
    List<Object> extract(Object instance, Class<?> type, List<FieldInfo> fields);
}
