package com.paramichha.testkit;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.model.TestFixture;

/**
 * Provides test data for a class — valid base, violations, boundary values.
 * Default implementation uses DataFactory. Implement to swap data source.
 */
public interface TestDataProvider {
    TestFixture provide(Class<?> type);

    TestFixture provide(Class<?> type, GenerationMode mode, Long seed);
}
