package com.paramichha.datafactory.fixtures;

import lombok.Builder;
import lombok.Value;

/**
 * Fixture 1 — all eight Java primitives, no annotations.
 * Exercises: FieldDescriptor extraction for primitive types,
 * isBooleanType(boolean.class), castNumeric for all primitive paths,
 * SetterStrategy / LombokBuilder with primitives.
 */
@Value
@Builder
public class PrimitivesRequest {
    byte byteVal;
    short shortVal;
    int intVal;
    long longVal;
    float floatVal;
    double doubleVal;
    char charVal;
    boolean boolVal;
}
