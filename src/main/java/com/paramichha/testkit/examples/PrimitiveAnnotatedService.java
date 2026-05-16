package com.paramichha.testkit.examples;

import com.paramichha.utils.DataValidation;
import jakarta.validation.constraints.*;

/**
 * Sample service — primitives WITH annotations.
 * Every param and return is a primitive with Jakarta constraints.
 * Guards enforced manually — throws IllegalArgumentException for invalid values.
 */
public class PrimitiveAnnotatedService {

    private @Min(1) @Max(100) int quantity;
    private @Min(0) @Max(999999) long price;
    private @Positive double rate;
    private @Min(0) @Max(127) byte level;
    private @PositiveOrZero short score;
    private boolean active;

    public PrimitiveAnnotatedService(@Min(1) @Max(100) int quantity,
                                     @Min(0) @Max(999999) long price,
                                     @Positive double rate,
                                     @Min(0) @Max(127) byte level,
                                     @PositiveOrZero short score,
                                     boolean active) {
        DataValidation.min(1, quantity);
        DataValidation.max(100, quantity);
        DataValidation.min(0L, price);
        DataValidation.max(999999L, price);
        DataValidation.positive(rate);
        DataValidation.min(0, (int) level);
        DataValidation.max(127, (int) level);
        DataValidation.positiveOrZero((int) score);
        this.quantity = quantity;
        this.price    = price;
        this.rate     = rate;
        this.level    = level;
        this.score    = score;
        this.active   = active;
    }

    // ── QUERY ─────────────────────────────────────────────────────────────

    public @Min(1) @Max(100) int getQuantity()  { return quantity; }
    public @Min(0) long getPrice()              { return price; }
    public @Positive double getRate()           { return rate; }
    public @Min(0) @Max(127) byte getLevel()    { return level; }
    public @PositiveOrZero short getScore()     { return score; }
    public boolean isActive()                   { return active; }

    // ── COMMAND ───────────────────────────────────────────────────────────

    public void reset() {
        this.quantity = 1;
        this.price    = 0L;
        this.rate     = 0.01;
        this.level    = 0;
        this.score    = 0;
        this.active   = false;
    }

    // ── MUTATOR ───────────────────────────────────────────────────────────

    public void setQuantity(@Min(1) @Max(100) int quantity) {
        DataValidation.min(1, quantity);
        DataValidation.max(100, quantity);
        this.quantity = quantity;
    }

    public void setRate(@Positive double rate) {
        DataValidation.positive(rate);
        this.rate = rate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // ── TRANSFORMER ───────────────────────────────────────────────────────

    public @Positive double applyDiscount(@Min(0) @Max(100) int discountPercent) {
        DataValidation.min(0, discountPercent);
        DataValidation.max(100, discountPercent);
        return rate * (1.0 - discountPercent / 100.0);
    }

    public @Min(0) long totalValue() {
        return price * quantity;
    }

    // ── OPERATION ─────────────────────────────────────────────────────────

    public @PositiveOrZero int combine(@Min(0) int a, @Min(0) int b) {
        DataValidation.min(0, a);
        DataValidation.min(0, b);
        return a + b;
    }

    public void transfer(@Min(1) int amount, @Min(0) int targetLevel) {
        DataValidation.min(1, amount);
        DataValidation.min(0, targetLevel);
        this.quantity -= amount;
        this.level    = (byte) targetLevel;
    }
}