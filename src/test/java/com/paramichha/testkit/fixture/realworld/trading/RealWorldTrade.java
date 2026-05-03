package com.paramichha.testkit.fixture.realworld.trading;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.realworld.trading.support.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Testable
public record RealWorldTrade(
        @NotBlank @Size(min = 1, max = 10) String symbol,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal price,
        @NotNull TradeSide side,
        @NotNull TradeStatus status,
        @NotNull @Future LocalDate settlementDate
) {}
