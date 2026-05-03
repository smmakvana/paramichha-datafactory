package com.paramichha.testkit.fixture.realworld.payment;

import com.paramichha.testkit.Testable;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Testable
public record RealWorldPayment(
        @NotBlank @Size(min = 15, max = 34) String senderIban,
        @NotBlank @Size(min = 15, max = 34) String receiverIban,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(min = 8, max = 11) String bic,
        @NotNull @Future LocalDate valueDate,
        @NotBlank @Size(max = 140) String reference
) {}
