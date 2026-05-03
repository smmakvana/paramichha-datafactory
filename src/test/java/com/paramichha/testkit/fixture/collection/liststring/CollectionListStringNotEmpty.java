package com.paramichha.testkit.fixture.collection.liststring;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;

@Testable
public record CollectionListStringNotEmpty(
        @NotEmpty List<String> items
) {}
