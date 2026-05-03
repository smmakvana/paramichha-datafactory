package com.paramichha.testkit.fixture.realworld.payment;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Testable
public record RealWorldPaymentDomestic(
        @NotBlank @Size(min = 6, max = 6) String sortCode,
        @NotBlank @Size(min = 8, max = 8) String accountNumber,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(max = 18) String reference
) {}
