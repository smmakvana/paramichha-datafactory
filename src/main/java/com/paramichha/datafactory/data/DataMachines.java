package com.paramichha.datafactory.data;

import net.datafaker.Faker;

import java.util.Locale;
import java.util.Random;

/**
 * Factory for {@link DataMachine} instances.
 *
 * <p>Two modes:
 * <pre>
 *   DataMachines.random()      — different values each run
 *   DataMachines.seeded(42L)   — same sequence for same seed, every run
 * </pre>
 *
 * <p>One machine is created per {@link com.paramichha.datafactory.DataBuilder}
 * and reused for all fields and objects built by that builder.
 * The machine is stateful — each call advances its internal position
 * in the deterministic sequence.
 *
 * <p>Only this class and {@link FakerDataMachine} import DataFaker.
 * To swap the library: implement {@link DataMachine} and update these two classes only.
 */
public final class DataMachines {

    private DataMachines() {}

    /**
     * Returns a random-mode machine.
     * Produces different values on every call — suitable for load test data.
     */
    public static DataMachine random() {
        return new FakerDataMachine(new Faker(Locale.ENGLISH), null);
    }

    /**
     * Returns a seeded machine.
     * Same seed → same sequence of values, every run.
     * The machine advances state with each call so successive fields
     * and objects get different but reproducible values.
     *
     * @param seed deterministic seed
     */
    public static DataMachine seeded(long seed) {
        Random rng = new Random(seed);
        return new FakerDataMachine(new Faker(Locale.ENGLISH, rng), rng);
    }
}
