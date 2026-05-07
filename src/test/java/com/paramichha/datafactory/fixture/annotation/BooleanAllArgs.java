package com.paramichha.example.annotation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class BooleanAllArgs {
    @AssertTrue boolean activePrimitive;
    @AssertTrue Boolean verifiedWrapper;
    @AssertFalse boolean deletedPrimitive;
    @AssertFalse Boolean archivedWrapper;
}
