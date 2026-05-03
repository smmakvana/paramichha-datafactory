package com.paramichha.testkit.fixture.annotation.length;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;

@Testable
public record AnnotationLengthString(
        @Length(min = 2, max = 50) String value
) {}
