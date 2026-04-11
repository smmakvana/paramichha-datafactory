package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
public class NumericPojo {
    @Min(1) @Max(127) byte tier;
    @Min(0) @Max(30000) short port;
    @Min(0) @Max(99999) int quantity;
    @Positive Long id;
    @PositiveOrZero Integer score;
    @Negative Integer debt;
    @NegativeOrZero Long adjustment;
    public NumericPojo() {}
    public byte getTier() { return tier; }
    public void setTier(byte tier) { this.tier = tier; }
    public short getPort() { return port; }
    public void setPort(short port) { this.port = port; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getDebt() { return debt; }
    public void setDebt(Integer debt) { this.debt = debt; }
    public Long getAdjustment() { return adjustment; }
    public void setAdjustment(Long adjustment) { this.adjustment = adjustment; }
}
