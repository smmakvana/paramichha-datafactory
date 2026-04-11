package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class ProductOrderAllArgs {
    @NotBlank String orderId;
    @NotEmpty List<String> items;
    @NotNull @Positive BigDecimal totalAmount;
    @NotNull @Past Instant placedAt;
    @NotNull ProductOrderRecord.OrderStatus status;

    public enum OrderStatus{DRAFT,SUBMITTED,APPROVED,SHIPPED}
}
