package com.paramichha.datafactory;

/**
 * Machine-readable outcome of a single DataFactory generation attempt.
 * Used in {@link DataFactoryResult} for single results
 * and derived automatically in {@link DataFactoryResults} for bulk results.
 */
public enum ResultStatus {

    /** Generation succeeded. Payload is present. */
    SUCCESS,

    /** Generation partially succeeded — some items built, some failed. Bulk only. */
    PARTIAL,

    /**
     * Generation failed. Payload is absent. Check {@link DataFactoryResult#errorType()} and
     * {@link DataFactoryResult#reason()}.
     */
    FAILED
}
