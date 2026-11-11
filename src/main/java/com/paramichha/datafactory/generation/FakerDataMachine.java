package com.paramichha.datafactory.generation;

import java.util.Random;
import java.util.UUID;
import net.datafaker.Faker;

/**
 * {@link DataMachine} backed by DataFaker.
 *
 * <p>Uses {@link DataHint} internally to map variable names to the
 * most appropriate Faker provider. {@link DataHint} is package-private —
 * this mapping is an internal concern, never exposed via the API.
 *
 * <p>Obtain via {@link DataMachines} — never construct directly.
 */
final class FakerDataMachine implements DataMachine {

    private final Faker faker;
    private final Random random;

    FakerDataMachine(Faker faker, Random random) {
        this.faker = faker;
        this.random = random;
    }

    @Override
    public String text(String hint) {
        // UUID is special — not a Faker YAML key, needs direct Random
        if (hint != null && hint.equalsIgnoreCase("uuid")) {
            return uuid();
        }
        // All other hints go through DataHint mapping
        return DataHint.fromVariableName(hint).produce(faker);
    }

    @Override
    public long number(long min, long max) {
        return faker.number().numberBetween(min, max);
    }

    @Override
    public double decimal(int scale, long min, long max) {
        return faker.number().randomDouble(scale, min, max);
    }

    @Override
    public int digit() {
        return faker.number().randomDigit();
    }

    @Override
    public String digits(int count) {
        return faker.number().digits(count);
    }

    private String uuid() {
        return random != null
                ? new UUID(random.nextLong(), random.nextLong()).toString()
                : UUID.randomUUID().toString();
    }
}
