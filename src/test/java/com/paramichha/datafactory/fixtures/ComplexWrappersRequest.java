package com.paramichha.datafactory.fixtures;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Fixture 7b — complex class built on top of AnnotatedWrappersRequest.
 * Exercises: nested @Valid wrapper request, list of wrapper requests,
 * AssertFalse on Boolean, combined @Email + @Size on the same String field.
 */
@Value
@Builder
public class ComplexWrappersRequest {
    @NotBlank
    String name;
    @NotNull
    @Valid
    AnnotatedWrappersRequest wrappers;
    @NotNull
    List<AnnotatedWrappersRequest> items;
    @AssertFalse
    Boolean deleted;
    @NotNull
    @NegativeOrZero
    Integer adjustment;
}
