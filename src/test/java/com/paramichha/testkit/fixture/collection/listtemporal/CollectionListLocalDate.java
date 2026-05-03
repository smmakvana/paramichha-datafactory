package com.paramichha.testkit.fixture.collection.listtemporal;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;

@Testable
public record CollectionListLocalDate(
        List<java.time.LocalDate> dates
) {}
