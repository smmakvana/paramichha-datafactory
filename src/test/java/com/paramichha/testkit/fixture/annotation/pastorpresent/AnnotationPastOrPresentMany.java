package com.paramichha.testkit.fixture.annotation.pastorpresent;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;

@Testable
public record AnnotationPastOrPresentMany(
        @PastOrPresent java.time.LocalDate registeredOn,
        @PastOrPresent java.time.LocalDateTime lastLogin
) {}
