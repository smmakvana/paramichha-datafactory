package com.paramichha.datafactory.fixtures;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Value;

/**
 * Fixture 4 — primitives with annotations.
 * Exercises: @Min/@Max on int/long/short/byte/float/double,
 *
 * @AssertTrue on boolean, castNumeric belowMin/aboveMax for all primitive types,
 * primitive null-skip in SetterStrategy.
 */
@Value
@Builder
public class AnnotatedPrimitivesRequest {
    @Min(1)
    @Max(127)
    byte byteVal;
    @Min(1)
    @Max(1000)
    short shortVal;
    @Min(0)
    @Max(9999)
    int intVal;
    @Min(0L)
    @Max(Long.MAX_VALUE / 2)
    long longVal;
    @Min(1)
    @Max(100)
    float floatVal;
    @Min(0)
    @Max(1000)
    double doubleVal;
    @AssertTrue
    boolean boolVal;
}
