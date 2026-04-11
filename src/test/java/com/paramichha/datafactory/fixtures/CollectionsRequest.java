package com.paramichha.datafactory.fixtures;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;

/**
 * Fixture 7c — collection fields as parameters.
 * Exercises: List<String>, List<Integer>, Collection<String>,
 * List of domain objects, isList(Collection.class) branch,
 * type-level default null+empty ConstraintCase for List/Collection fields.
 */
@Value
@Builder
public class CollectionsRequest {
    @NotBlank
    String name;
    List<String> tags;
    List<Integer> scores;
    Collection<String> labels;
    List<AnnotatedWrappersRequest> items;
}
