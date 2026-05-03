package com.paramichha.testkit.fixture.annotation.size;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationSizeListMany(
        @Size(min = 1) java.util.List<String> tags,
        @Size(max = 5) java.util.List<Integer> scores
) {}
