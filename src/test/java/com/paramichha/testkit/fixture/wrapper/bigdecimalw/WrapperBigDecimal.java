package com.paramichha.testkit.fixture.wrapper.bigdecimalw;

import com.paramichha.testkit.Testable;
import java.math.BigDecimal;

@Testable
public record WrapperBigDecimal(
        BigDecimal val
) {}
