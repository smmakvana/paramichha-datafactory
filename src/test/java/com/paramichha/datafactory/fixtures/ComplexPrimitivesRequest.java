package com.paramichha.datafactory.fixtures;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Fixture 7a — complex class built on top of AnnotatedPrimitivesRequest.
 * Exercises: nested @Valid, List of domain objects, List of Strings,
 * resolveAllValid nested branch, resolveAllValid list-nested branch.
 */
@Value
@Builder
public class ComplexPrimitivesRequest {
    @NotBlank
    String name;
    @NotNull
    @Valid
    AnnotatedPrimitivesRequest primitives;
    @NotNull
    List<AnnotatedPrimitivesRequest> primitiveSets;
    List<String> tags;
    @NotNull
    @Min(1)
    @Max(10)
    Integer priority;
}
