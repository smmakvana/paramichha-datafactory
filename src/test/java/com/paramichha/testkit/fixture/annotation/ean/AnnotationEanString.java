package com.paramichha.testkit.fixture.annotation.ean;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.*;

@Testable
public record AnnotationEanString(
        @EAN String ean
) {}
