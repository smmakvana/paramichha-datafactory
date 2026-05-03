package com.paramichha.testkit.fixture.temporal.localdate;

import com.paramichha.testkit.Testable;
import java.time.LocalDate;

@Testable
public record TemporalLocalDate(
        LocalDate value
) {}
