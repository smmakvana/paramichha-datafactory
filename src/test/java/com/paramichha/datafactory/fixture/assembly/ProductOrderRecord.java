package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record ProductOrderRecord(@NotBlank String orderId, @NotEmpty List<String> items, @NotNull @Positive BigDecimal totalAmount, @NotNull @Past Instant placedAt, @NotNull ProductOrderRecord.OrderStatus status) {
    public enum OrderStatus{DRAFT,SUBMITTED,APPROVED,SHIPPED}
}
