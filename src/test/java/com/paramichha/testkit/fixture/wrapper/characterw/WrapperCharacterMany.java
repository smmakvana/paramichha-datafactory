package com.paramichha.testkit.fixture.wrapper.characterw;

import com.paramichha.testkit.Testable;


@Testable
public record WrapperCharacterMany(
        Character c1,
        Character c2,
        Character c3
) {}
