package com.paramichha.datafactory;

/**
 * Controls advisory warnings emitted by DataFactory when annotation contracts
 * look incomplete for production use.
 *
 * <p>DataFactory always works only with declared annotations — no injection happens
 * regardless of mode. Mode is purely a signal for whether to surface warnings.
 *
 * <h2>PRODUCTION (default)</h2>
 * <p>Logs advisory warnings to stdout when a field's declared annotations look
 * incomplete — e.g. {@code @Email} without {@code @NotNull}, or a numeric field
 * with no {@code @Min}/{@code @Max}. Warnings do not stop generation.
 *
 * <h2>DEV</h2>
 * <p>Silent — no warnings. Use during active development when annotations
 * are still being added and incomplete contracts are expected.
 */
public enum GenerationMode {

    /**
     * Production-grade generation. Safe defaults for unannotated fields.
     * This is the default — you never need to set it explicitly.
     */
    PRODUCTION,

    /**
     * Development-mode generation. No injected null constraints.
     * Full numeric type ranges for unannotated number fields.
     */
    DEV
}
