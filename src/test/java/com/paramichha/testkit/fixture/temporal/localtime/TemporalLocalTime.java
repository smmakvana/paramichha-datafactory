package com.paramichha.testkit.fixture.temporal.localtime;

import com.paramichha.testkit.Testable;
import java.time.LocalTime;

@Testable
public record TemporalLocalTime(
        LocalTime value
) {}
