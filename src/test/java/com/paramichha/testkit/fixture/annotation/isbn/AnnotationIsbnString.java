package com.paramichha.testkit.fixture.annotation.isbn;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;

@Testable
public record AnnotationIsbnString(
        @ISBN String isbn
) {}
