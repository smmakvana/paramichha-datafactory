package com.paramichha.testkit.fixture.collection.listuuid;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;

@Testable
public record CollectionListUuid(
        List<java.util.UUID> ids
) {}
