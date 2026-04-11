package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.hibernate.validator.constraints.CreditCardNumber;
public record PaymentCardRecord(@NotBlank @CreditCardNumber String cardNumber, @NotNull @FutureOrPresent YearMonth expiryDate, @NotBlank @Pattern(regexp="\\d{3,4}") String cvv, @NotNull @Positive BigDecimal amount, @NotBlank @Size(min=3,max=3) String currency) {}
