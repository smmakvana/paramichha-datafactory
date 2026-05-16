package com.paramichha.testkit.examples;

import com.paramichha.utils.DataValidation;
import jakarta.validation.constraints.*;

/**
 * Sample service — mix of annotated and plain primitives.
 * Some params/returns constrained, some not.
 * TestKit generates reject tests only for constrained params.
 */
public class PrimitiveMixedService {

    private @Min(1) @Max(1000) int quantity;
    private long price;
    private @Positive double rate;
    private byte level;
    private boolean active;

    public PrimitiveMixedService(@Min(1) @Max(1000) int quantity,
                                 long price,
                                 @Positive double rate,
                                 byte level,
                                 boolean active) {
        DataValidation.min(1, quantity);
        DataValidation.max(1000, quantity);
        DataValidation.positive(rate);
        this.quantity = quantity;
        this.price    = price;
        this.rate     = rate;
        this.level    = level;
        this.active   = active;
    }

    // ── QUERY — mix of constrained and plain returns ───────────────────────

    public @Min(1) @Max(1000) int getQuantity() { return quantity; }
    public long getPrice()                       { return price; }
    public @Positive double getRate()            { return rate; }
    public byte getLevel()                       { return level; }
    public boolean isActive()                    { return active; }

    // ── COMMAND ───────────────────────────────────────────────────────────

    public void reset() {
        this.quantity = 1;
        this.price    = 0L;
        this.rate     = 0.01;
        this.level    = 0;
        this.active   = false;
    }

    // ── MUTATOR — constrained + plain mix ─────────────────────────────────

    public void setQuantity(@Min(1) @Max(1000) int quantity) {
        DataValidation.min(1, quantity);
        DataValidation.max(1000, quantity);
        this.quantity = quantity;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setRate(@Positive double rate) {
        DataValidation.positive(rate);
        this.rate = rate;
    }

    // ── TRANSFORMER — annotated param, plain return ────────────────────────

    public double applyDiscount(@Min(0) @Max(100) int discountPercent) {
        DataValidation.min(0, discountPercent);
        DataValidation.max(100, discountPercent);
        return rate * (1.0 - discountPercent / 100.0);
    }

    // ── TRANSFORMER — plain param, annotated return ────────────────────────

    public @Positive double scaledRate(int factor) {
        return rate * Math.max(1, factor);
    }

    // ── OPERATION — mix of constrained and plain params ────────────────────

    public int combine(@Min(0) int constrained, int plain) {
        DataValidation.min(0, constrained);
        return constrained + plain;
    }

    public void transfer(@Min(1) int amount, int targetLevel) {
        DataValidation.min(1, amount);
        this.quantity -= amount;
        this.level    = (byte) targetLevel;
    }
}