package com.paramichha.testkit;

import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.TestData;

/**
 * Generates a {@link GeneratedTestClass} from class metadata and test data.
 * Implement to produce a different test style (e.g. Spock, Kotest).
 */
public interface TestGenerator {
    GeneratedTestClass generate(ClassMeta meta, TestData data, String targetPackage);
}
