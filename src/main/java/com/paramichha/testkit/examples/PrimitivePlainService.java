package com.paramichha.testkit.examples;

/**
 * Sample service — primitives WITHOUT annotations.
 * No Jakarta constraints anywhere. Any primitive value is valid.
 * TestKit generates valid tests only — no reject tests.
 */
public class PrimitivePlainService {

    private int quantity;
    private long price;
    private double rate;
    private byte level;
    private short score;
    private boolean active;

    public PrimitivePlainService(int quantity, long price, double rate,
                                 byte level, short score, boolean active) {
        this.quantity = quantity;
        this.price    = price;
        this.rate     = rate;
        this.level    = level;
        this.score    = score;
        this.active   = active;
    }

    // QUERY
    public int    getQuantity() { return quantity; }
    public long   getPrice()    { return price; }
    public double getRate()     { return rate; }
    public byte   getLevel()    { return level; }
    public short  getScore()    { return score; }
    public boolean isActive()   { return active; }

    // COMMAND
    public void reset() {
        this.quantity = 0;
        this.price    = 0L;
        this.rate     = 0.0;
        this.level    = 0;
        this.score    = 0;
        this.active   = false;
    }

    // MUTATOR
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setRate(double rate)      { this.rate = rate; }
    public void setActive(boolean active) { this.active = active; }

    // TRANSFORMER
    public double applyDiscount(int discountPercent) {
        return rate * (1.0 - discountPercent / 100.0);
    }

    public long totalValue() {
        return price * quantity;
    }

    // OPERATION
    public int combine(int a, int b)        { return a + b; }
    public void transfer(int amount, int target) { this.quantity -= amount; }
}
