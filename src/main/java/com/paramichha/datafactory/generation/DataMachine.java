package com.paramichha.datafactory.generation;

/**
 * Produces realistic data values for field generation.
 *
 * <p>One instance per {@link com.paramichha.datafactory.DataBuilder}.
 * Stateful — advances internal state with each call.
 *
 * <p>Obtained via {@link DataMachines#random()} or {@link DataMachines#seeded(long)}.
 *
 * <h2>Replacing the implementation</h2>
 * <p>Implement this interface to swap the underlying library.
 * Only {@link FakerDataMachine} and {@link DataMachines} need to change.
 */
public interface DataMachine {

    /**
     * Produces a realistic string for the given hint.
     * The hint is typically a field name or semantic category.
     * Unknown hints return a random word — never throws.
     *
     * @param hint field name or category e.g. "email", "city", "creditCard"
     * @return a non-null realistic string
     */
    String text(String hint);

    /** Produces a long value in [{@code min}, {@code max}]. */
    long number(long min, long max);

    /** Produces a double in [{@code min}, {@code max}] with {@code scale} decimal places. */
    double decimal(int scale, long min, long max);

    /** Produces a single random digit 0–9. */
    int digit();

    /** Produces a string of exactly {@code count} random digits. */
    String digits(int count);
}
