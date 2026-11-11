package com.paramichha.datafactory.generation;

import java.util.Locale;
import java.util.Random;
import net.datafaker.Faker;

/**
 * Factory for {@link DataMachine} instances.
 *
 * <pre>
 *   DataMachines.random()      — different values each run
 *   DataMachines.seeded(42L)   — same sequence for same seed, every run
 * </pre>
 *
 * <p>Only this class and {@link FakerDataMachine} import DataFaker.
 * To swap the library: implement {@link DataMachine} and update only these two classes.
 */
public final class DataMachines {

    private DataMachines() {}

    /** Returns a random-mode machine. Different values every call. */
    public static DataMachine random() {
        return new FakerDataMachine(new Faker(Locale.ENGLISH), null);
    }

    /**
     * Returns a seeded machine.
     * Same seed → same sequence of values, every run.
     */
    public static DataMachine seeded(long seed) {
        Random rng = new Random(seed);
        return new FakerDataMachine(new Faker(Locale.ENGLISH, rng), rng);
    }
}
