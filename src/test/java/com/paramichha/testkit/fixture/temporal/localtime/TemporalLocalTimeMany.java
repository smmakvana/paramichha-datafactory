package com.paramichha.testkit.fixture.temporal.localtime;

import com.paramichha.testkit.Testable;
import java.time.LocalTime;

@Testable
public record TemporalLocalTimeMany(
        LocalTime value1,
        LocalTime value2,
        LocalTime value3
) {}
