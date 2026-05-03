package com.paramichha.testkit.fixture.collection.listpojo;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;

@Testable
public record CollectionListPojo(
        @NotBlank String owner,
        java.util.List<CollectionItem> items
) {}
