package com.paramichha.testkit.fixture.annotation.url;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;

@Testable
public record AnnotationUrlMany(
        @URL String website,
        @URL String apiEndpoint
) {}
