package com.paramichha.testkit.fixture.annotation.notnull;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNotNullByte(
        @NotNull Byte         value
) {}
