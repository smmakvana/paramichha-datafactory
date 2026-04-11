package com.paramichha.datafactory.fixture.type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
public record ValuesRecord(Byte byteVal, Short shortVal, Integer intVal, Long longVal, Float floatVal, Double doubleVal, Character charVal, Boolean boolVal, BigDecimal bigDecimalVal, BigInteger bigIntegerVal, UUID uuidVal, String stringVal) {}
