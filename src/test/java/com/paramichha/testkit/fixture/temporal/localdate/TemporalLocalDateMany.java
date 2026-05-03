package com.paramichha.testkit.fixture.temporal.localdate;

import com.paramichha.testkit.Testable;
import java.time.LocalDate;

@Testable
public record TemporalLocalDateMany(
        LocalDate value1,
        LocalDate value2,
        LocalDate value3
) {}
