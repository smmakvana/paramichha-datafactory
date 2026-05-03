package com.paramichha.testkit.fixture.annotation.size;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationSizeList(
        @Size(min = 1, max = 10) java.util.List<String> items
) {}
