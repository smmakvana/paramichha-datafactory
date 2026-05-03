package com.paramichha.testkit.fixture.annotation.notempty;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotEmptyMany(
        @NotEmpty String name,
        @NotEmpty java.util.List<String> tags
) {}
