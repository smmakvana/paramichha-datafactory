package com.paramichha.datafactory.fixtures;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

/**
 * Fixture 2 — all Java wrapper types plus BigDecimal, BigInteger, UUID.
 * No annotations — exercises semantic default generation for every numeric wrapper,
 * UUID shaping, and toSourceCode for all wrapper branches.
 */
@Value
@Builder
public class WrappersRequest {
    Byte byteVal;
    Short shortVal;
    Integer intVal;
    Long longVal;
    Float floatVal;
    Double doubleVal;
    Character charVal;
    Boolean boolVal;
    BigDecimal bigDecimalVal;
    BigInteger bigIntegerVal;
    UUID uuidVal;
    String stringVal;
}
