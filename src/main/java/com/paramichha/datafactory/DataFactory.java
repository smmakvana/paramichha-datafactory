package com.paramichha.datafactory;

/**
 * Entry point for the DataFactory library.
 *
 * @see DataBuilder for the full API reference
 */
public final class DataFactory {

    private DataFactory() {
    }

    public static <T> DataBuilder<T> of(Class<T> type) {
        return new DefaultDataBuilder<>(type);
    }
}
