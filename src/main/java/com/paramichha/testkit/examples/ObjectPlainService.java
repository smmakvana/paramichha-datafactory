package com.paramichha.testkit.examples;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sample service — object types WITHOUT annotations.
 * No Jakarta constraints anywhere. Any value including null is valid input.
 * TestKit generates valid tests only — no reject tests.
 * Methods handle null gracefully — no contract declared.
 */
public class ObjectPlainService {

    private String name;
    private BigDecimal price;
    private LocalDate createdDate;
    private String email;

    public ObjectPlainService(String name, BigDecimal price,
                              LocalDate createdDate, String email) {
        this.name        = name;
        this.price       = price;
        this.createdDate = createdDate;
        this.email       = email;
    }

    // ── QUERY ─────────────────────────────────────────────────────────────

    public String    getName()        { return name; }
    public BigDecimal getPrice()      { return price; }
    public LocalDate getCreatedDate() { return createdDate; }
    public String    getEmail()       { return email; }

    // ── COMMAND ───────────────────────────────────────────────────────────

    public void reset() {
        this.name  = null;
        this.price = BigDecimal.ZERO;
    }

    // ── MUTATOR ───────────────────────────────────────────────────────────

    public void setName(String name)       { this.name = name; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setEmail(String email)     { this.email = email; }

    // ── TRANSFORMER — null-safe implementations ────────────────────────────

    public String formatDisplay(String prefix) {
        return (prefix != null ? prefix : "") + ": " + name;
    }

    public BigDecimal applyTax(BigDecimal rate) {
        if (price == null || rate == null) return BigDecimal.ZERO;
        return price.multiply(rate.add(BigDecimal.ONE));
    }

    // ── OPERATION — null-safe implementations ─────────────────────────────

    public ObjectPlainService merge(ObjectPlainService other, String newName) {
        BigDecimal mergedPrice = (price != null && other != null && other.price != null)
                ? price.add(other.price) : BigDecimal.ZERO;
        return new ObjectPlainService(newName, mergedPrice, LocalDate.now(), this.email);
    }

    public void copyFrom(ObjectPlainService source, String reason) {
        if (source == null) return;
        this.name  = source.name;
        this.price = source.price;
    }
}