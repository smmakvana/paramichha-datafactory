package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
public record BooleanRecord(@AssertTrue boolean activePrimitive, @AssertTrue Boolean verifiedWrapper, @AssertFalse boolean deletedPrimitive, @AssertFalse Boolean archivedWrapper) {}
