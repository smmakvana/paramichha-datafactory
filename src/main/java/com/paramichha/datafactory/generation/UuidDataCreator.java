package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.FieldConstraints;

import java.util.UUID;

/**
 * Produces UUID values.
 * When {@link FakerProvider#SEEDED_RANDOM} is set (seed active), output is deterministic.
 * Otherwise produces random UUIDs via UUID.randomUUID().
 */
final class UuidDataCreator implements DataCreator {

    static final UuidDataCreator INSTANCE = new UuidDataCreator();

    private UuidDataCreator() {}

    @Override
    public boolean supports(Class<?> type) {
        return type == UUID.class;
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target) {
        return shape(field, target, FakerProvider.random());
    }

    @Override
    public Object shape(FieldConstraints field, BoundaryTarget target, net.datafaker.Faker faker) {
        java.util.Random rng = FakerProvider.SEEDED_RANDOM.get();
        if (rng != null) {
            return new UUID(rng.nextLong(), rng.nextLong());
        }
        return UUID.randomUUID();
    }
}