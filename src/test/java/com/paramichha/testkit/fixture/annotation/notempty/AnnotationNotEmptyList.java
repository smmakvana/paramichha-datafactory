package com.paramichha.testkit.fixture.annotation.notempty;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.List;

@Testable
public record AnnotationNotEmptyList(
        @NotEmpty java.util.List<String> items
) {}
