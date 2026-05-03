package com.paramichha.testkit.fixture.annotation.nullconstraint;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationNullInteger(
        @Null Integer value
) {}
