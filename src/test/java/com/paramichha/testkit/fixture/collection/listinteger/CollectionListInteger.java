package com.paramichha.testkit.fixture.collection.listinteger;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;

@Testable
public record CollectionListInteger(
        List<Integer> scores
) {}
