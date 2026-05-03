package com.paramichha.testkit.fixture.wrapper.bigintegerw;

import com.paramichha.testkit.Testable;
import java.math.BigInteger;

@Testable
public record WrapperBigIntegerMany(
        BigInteger val1,
        BigInteger val2,
        BigInteger val3
) {}
