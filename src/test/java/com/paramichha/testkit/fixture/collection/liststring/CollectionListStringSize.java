package com.paramichha.testkit.fixture.collection.liststring;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;

@Testable
public record CollectionListStringSize(
        @Size(min = 1, max = 10) List<String> items
) {}
