package com.paramichha.testkit.fixture.wrapper.bigdecimalw;

import com.paramichha.testkit.Testable;
import java.math.BigDecimal;

@Testable
public record WrapperBigDecimalMany(
        BigDecimal val1,
        BigDecimal val2,
        BigDecimal val3
) {}
