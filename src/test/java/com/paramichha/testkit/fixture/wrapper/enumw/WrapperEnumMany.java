package com.paramichha.testkit.fixture.wrapper.enumw;

import com.paramichha.testkit.Testable;


@Testable
public record WrapperEnumMany(
        FixtureStatus status,
        FixtureStatus previousStatus,
        FixtureStatus nextStatus
) {}
