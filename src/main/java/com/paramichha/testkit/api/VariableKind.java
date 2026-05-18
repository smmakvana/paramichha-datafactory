package com.paramichha.testkit.api;

/**
 * Semantic category of any variable — field, parameter, or return type.
 * Drives assertion and generation logic.
 * No more instanceof checks or Class.isPrimitive() scattered in writers.
 */
public enum VariableKind {
    PRIMITIVE,   // int, long, boolean, byte, short, double, float, char
    STRING,      // java.lang.String
    NUMERIC,     // BigDecimal, BigInteger
    TEMPORAL,    // LocalDate, LocalDateTime, Instant, ZonedDateTime, Date
    BOOLEAN,     // Boolean (boxed)
    UUID,        // java.util.UUID
    ENUM,        // any enum type
    COLLECTION,  // List, Set, Queue, any Iterable
    MAP,         // Map and subtypes
    ARRAY,       // T[]
    OPTIONAL,    // Optional<T>
    DOMAIN,      // user-defined class/record — not java.* or jakarta.*
    VOID,        // void / Void
    UNKNOWN      // anything else
}
