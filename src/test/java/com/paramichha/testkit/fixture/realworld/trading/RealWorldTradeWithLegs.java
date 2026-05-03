package com.paramichha.testkit.fixture.realworld.trading;

import com.paramichha.testkit.Testable;
import com.paramichha.testkit.fixture.realworld.trading.support.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Testable
public record RealWorldTradeWithLegs(
        @NotBlank @Size(max = 50) String tradeId,
        @NotNull @Positive BigDecimal totalValue,
        @NotNull TradeStatus status,
        @NotNull @Size(min = 1) @Valid List<TradeLeg> legs
) {}
