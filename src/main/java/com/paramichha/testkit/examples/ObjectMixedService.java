package com.paramichha.testkit.examples;

import com.paramichha.utils.DataValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Sample service — mix of annotated and plain object types.
 * Some params/returns constrained, some not.
 */
public class ObjectMixedService {

    private @NotBlank String name;
    private BigDecimal price;          // no constraint
    private @NotNull LocalDate createdDate;
    private String email;              // no constraint

    public ObjectMixedService(@NotBlank String name,
                               BigDecimal price,
                               @NotNull LocalDate createdDate,
                               String email) {
        this.name        = DataValidation.notBlank(name);
        this.price       = price;
        this.createdDate = Objects.requireNonNull(createdDate, "createdDate must not be null");
        this.email       = email;
    }

    // QUERY — mix
    public @NotBlank String getName()       { return name; }
    public BigDecimal getPrice()            { return price; }   // plain
    public @NotNull LocalDate getCreatedDate() { return createdDate; }
    public String getEmail()               { return email; }    // plain

    // COMMAND
    public void reset() {
        this.price = BigDecimal.ZERO;
        this.email = null;
    }

    // MUTATOR — constrained + plain mix
    public void setName(@NotBlank String name) {
        this.name = DataValidation.notBlank(name);
    }

    public void setPrice(BigDecimal price) { this.price = price; } // plain

    public void setEmail(@Email String email) {
        this.email = DataValidation.email(email);
    }

    // TRANSFORMER — annotated param, plain return
    public String formatDisplay(@NotBlank String prefix) {
        DataValidation.notBlank(prefix);
        return prefix + ": " + name;
    }

    // TRANSFORMER — plain param, annotated return
    public @NotNull BigDecimal resolvedPrice(BigDecimal fallback) {
        return price != null ? price : fallback != null ? fallback : BigDecimal.ZERO;
    }

    // OPERATION — mix
    @Valid
    public ObjectMixedService merge(@NotNull ObjectMixedService other, String optionalLabel) {
        DataValidation.notNull(other);
        String merged = optionalLabel != null ? optionalLabel : this.name;
        return new ObjectMixedService(merged,
                this.price != null && other.price != null
                        ? this.price.add(other.price) : BigDecimal.ZERO,
                LocalDate.now(), this.email);
    }

    public void copyFrom(@NotNull ObjectMixedService source, String reason) {
        DataValidation.notNull(source);
        this.name  = source.name;
        this.price = source.price;
    }
}
