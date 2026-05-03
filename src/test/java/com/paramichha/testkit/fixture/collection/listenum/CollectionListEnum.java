package com.paramichha.testkit.fixture.collection.listenum;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.wrapper.enumw.FixtureStatus;
import jakarta.validation.constraints.*;
import java.util.*;

/**
 * Fixture: collection containing enum wrapper type.
 * Note: uses NotNull on a single enum field alongside the list.
 */
@Testable
public record CollectionListEnum(
        @NotNull FixtureStatus status,
        List<String> labels
) {}
