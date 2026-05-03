package com.paramichha.testkit.fixture.collection.listbigdecimal;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.util.*;
import java.math.BigDecimal;

@Testable
public record CollectionListBigDecimal(
        List<java.math.BigDecimal> amounts
) {}
