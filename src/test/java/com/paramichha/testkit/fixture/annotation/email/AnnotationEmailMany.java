package com.paramichha.testkit.fixture.annotation.email;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationEmailMany(
        @Email String primaryEmail,
        @Email String secondaryEmail
) {}
