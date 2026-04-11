package com.paramichha.datafactory.generation;

import net.datafaker.Faker;

import java.util.Locale;
import java.util.Random;

/**
 * Single source of truth for Faker instances and seeded Random access.
 *
 * SEEDED_RANDOM is set by DefaultFieldBuilder before value generation
 * when a seed is active. UuidTypeShaper reads it to produce deterministic UUIDs.
 * Cleared in finally block after generation — never leaks between calls.
 */
public final class FakerProvider {

    private static final ThreadLocal<Faker> RANDOM_FAKER =
            ThreadLocal.withInitial(() -> new Faker(Locale.ENGLISH));

    /** Set by DefaultFieldBuilder when seed is active. Used by UuidTypeShaper. */
    public static final ThreadLocal<Random> SEEDED_RANDOM = new ThreadLocal<>();

    private FakerProvider() {}

    public static Faker forSeed(long seed) {
        return new Faker(Locale.ENGLISH, new Random(seed));
    }

    public static Random randomForSeed(long seed) {
        return new Random(seed);
    }

    public static Faker random() {
        return RANDOM_FAKER.get();
    }
}