package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class NumericAllArgs {
    @Min(1) @Max(127) byte tier;
    @Min(0) @Max(30000) short port;
    @Min(0) @Max(99999) int quantity;
    @Positive Long id;
    @PositiveOrZero Integer score;
    @Negative Integer debt;
    @NegativeOrZero Long adjustment;
}
