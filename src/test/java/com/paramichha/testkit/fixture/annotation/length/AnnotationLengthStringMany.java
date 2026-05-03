package com.paramichha.testkit.fixture.annotation.length;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;

@Testable
public record AnnotationLengthStringMany(
        @Length(min = 2, max = 50) String firstName,
        @Length(max = 100) String bio
) {}
