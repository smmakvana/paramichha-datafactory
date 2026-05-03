package com.paramichha.testkit.fixture.wrapper.uuidw;

import com.paramichha.testkit.Testable;


@Testable
public record WrapperUuidMany(
        java.util.UUID id1,
        java.util.UUID id2,
        java.util.UUID id3
) {}
