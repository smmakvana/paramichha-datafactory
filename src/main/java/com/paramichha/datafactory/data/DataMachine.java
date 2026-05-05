package com.paramichha.datafactory.data;


/**
 * Produces realistic data values for field generation.
 *
 * <p>One instance is created per {@link com.paramichha.datafactory.DataBuilder}
 * and reused across all fields and all objects built by that builder.
 * The machine advances its internal state with each call — producing
 * a deterministic sequence when seeded, or a random sequence otherwise.
 *
 * <p>Obtained via {@link DataMachines#random()} or {@link DataMachines#seeded(long)}.
 *
 * <h2>Text hints</h2>
 * <pre>
 *   text("email")      → realistic email address
 *   text("name")       → full name
 *   text("city")       → city name
 *   text("creditCard") → credit card number
 *   text("uuid")       → UUID string
 *   text("word")       → random word (default for unknown hints)
 * </pre>
 */
public interface DataMachine {

    /**
     * Produces a realistic string for the given semantic hint.
     * Unknown hints fall back to a random word — never throws.
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
