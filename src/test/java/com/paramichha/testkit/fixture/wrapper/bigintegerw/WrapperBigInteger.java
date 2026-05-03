package com.paramichha.testkit.fixture.wrapper.bigintegerw;

import com.paramichha.testkit.Testable;
import java.math.BigInteger;

@Testable
public record WrapperBigInteger(
        BigInteger val
) {}
