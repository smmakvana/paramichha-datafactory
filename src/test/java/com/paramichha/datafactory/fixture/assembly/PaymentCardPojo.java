package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.hibernate.validator.constraints.CreditCardNumber;
public class PaymentCardPojo {
    @NotBlank @CreditCardNumber String cardNumber;
    @NotNull @FutureOrPresent YearMonth expiryDate;
    @NotBlank @Pattern(regexp="\\d{3,4}") String cvv;
    @NotNull @Positive BigDecimal amount;
    @NotBlank @Size(min=3,max=3) String currency;
    public PaymentCardPojo() {}
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public YearMonth getExpiryDate() { return expiryDate; }
    public void setExpiryDate(YearMonth expiryDate) { this.expiryDate = expiryDate; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
