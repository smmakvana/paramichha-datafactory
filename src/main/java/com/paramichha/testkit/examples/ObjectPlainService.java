package com.paramichha.testkit.examples;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sample service — object types WITHOUT annotations.
 * No Jakarta constraints. TestKit generates valid tests only.
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

    // QUERY
    public String getName()          { return name; }
    public BigDecimal getPrice()     { return price; }
    public LocalDate getCreatedDate() { return createdDate; }
    public String getEmail()         { return email; }

    // COMMAND
    public void reset() {
        this.name  = null;
        this.price = BigDecimal.ZERO;
    }

    // MUTATOR
    public void setName(String name)            { this.name = name; }
    public void setPrice(BigDecimal price)      { this.price = price; }
    public void setEmail(String email)          { this.email = email; }

    // TRANSFORMER
    public String formatDisplay(String prefix)  { return prefix + ": " + name; }
    public BigDecimal applyTax(BigDecimal rate) { return price.multiply(rate.add(BigDecimal.ONE)); }

    // OPERATION
    public ObjectPlainService merge(ObjectPlainService other, String newName) {
        return new ObjectPlainService(newName,
                this.price.add(other.price), LocalDate.now(), this.email);
    }

    public void copyFrom(ObjectPlainService source, String reason) {
        this.name  = source.name;
        this.price = source.price;
    }
}
