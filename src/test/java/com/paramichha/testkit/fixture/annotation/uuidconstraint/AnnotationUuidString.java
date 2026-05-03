package com.paramichha.testkit.fixture.annotation.uuidconstraint;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationUuidString(
        @org.hibernate.validator.constraints.UUID String uuid
) {}
