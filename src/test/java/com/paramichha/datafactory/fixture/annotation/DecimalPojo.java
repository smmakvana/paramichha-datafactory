package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class DecimalPojo {
    @NotNull @DecimalMin("0.00") @DecimalMax("999999.99") BigDecimal price;
    @NotNull @Digits(integer=8,fraction=2) BigDecimal amount;
    @NotNull @Positive BigDecimal tax;
    @NotNull @PositiveOrZero Double rating;
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double probability;
    @NotBlank @DecimalMin("0.01") String minAmount;
    public DecimalPojo() {}
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Double getProbability() { return probability; }
    public void setProbability(Double probability) { this.probability = probability; }
    public String getMinAmount() { return minAmount; }
    public void setMinAmount(String minAmount) { this.minAmount = minAmount; }
}
