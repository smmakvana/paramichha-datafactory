package com.paramichha.testkit.fixture.collection.listpojo;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;

@Testable
public record CollectionListPojoNotNull(
        @NotBlank String owner,
        @NotNull java.util.List<CollectionItem> items
) {}
