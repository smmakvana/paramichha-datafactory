package com.paramichha.testkit;

import com.paramichha.testkit.model.TestData;

/**
 * Provides test data for a class — valid base, violations, boundary values.
 * Default implementation uses DataFactory. Implement to swap data source.
 */
public interface TestDataProvider {
    TestData provide(Class<?> type);
}
