package com.paramichha.testkit.fixture.annotation.email;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationEmailString(
        @Email String email
) {}
