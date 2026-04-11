package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class BooleanLombok {
    @AssertTrue boolean activePrimitives;
    @AssertTrue Boolean verifiedWrapper;
    @AssertFalse boolean deletedPrimitive;
    @AssertFalse Boolean archivedWrapper;
}
