package com.paramichha.datafactory.fixture.assembly;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public class ProductOrderPojo {
    @NotBlank String orderId;
    @NotEmpty List<String> items;
    @NotNull @Positive BigDecimal totalAmount;
    @NotNull @Past Instant placedAt;
    @NotNull ProductOrderRecord.OrderStatus status;
    public ProductOrderPojo() {}
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Instant getPlacedAt() { return placedAt; }
    public void setPlacedAt(Instant placedAt) { this.placedAt = placedAt; }
    public ProductOrderRecord.OrderStatus getStatus() { return status; }
    public void setStatus(ProductOrderRecord.OrderStatus status) { this.status = status; }

    public enum OrderStatus{DRAFT,SUBMITTED,APPROVED,SHIPPED}
}
