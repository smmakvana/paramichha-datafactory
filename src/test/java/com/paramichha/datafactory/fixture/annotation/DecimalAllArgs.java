package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class DecimalAllArgs {
    @NotNull @DecimalMin("0.00") @DecimalMax("999999.99") BigDecimal price;
    @NotNull @Digits(integer=8,fraction=2) BigDecimal amount;
    @NotNull @Positive BigDecimal tax;
    @NotNull @PositiveOrZero Double rating;
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double probability;
    @NotBlank @DecimalMin("0.01") String minAmount;
}
