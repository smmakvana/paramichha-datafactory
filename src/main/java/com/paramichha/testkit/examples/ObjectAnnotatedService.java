package com.paramichha.testkit.examples;

import com.paramichha.utils.DataValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Sample service — object types WITH annotations.
 * String, BigDecimal, LocalDate — all constrained.
 */
public class ObjectAnnotatedService {

    private @NotBlank String name;
    private @NotNull @Positive BigDecimal price;
    private @NotNull @PastOrPresent LocalDate createdDate;
    private @Email String email;

    public ObjectAnnotatedService(@NotBlank String name,
                                  @NotNull @Positive BigDecimal price,
                                  @NotNull @PastOrPresent LocalDate createdDate,
                                  @Email String email) {
        this.name        = DataValidation.notBlank(name);
        this.price       = DataValidation.positive(DataValidation.notNull(price));
        this.createdDate = DataValidation.pastOrPresent(DataValidation.notNull(createdDate));
        this.email       = DataValidation.email(email);
    }

    // QUERY
    public @NotBlank String getName()                 { return name; }
    public @NotNull @Positive BigDecimal getPrice()   { return price; }
    public @NotNull LocalDate getCreatedDate()        { return createdDate; }
    public @Email String getEmail()                   { return email; }

    // COMMAND
    public void reset() {
        this.name  = "unknown";
        this.price = BigDecimal.ONE;
    }

    // MUTATOR
    public void setName(@NotBlank String name) {
        this.name = DataValidation.notBlank(name);
    }

    public void setPrice(@NotNull @Positive BigDecimal price) {
        this.price = DataValidation.positive(DataValidation.notNull(price));
    }

    public void setEmail(@Email String email) {
        this.email = DataValidation.email(email);
    }

    // TRANSFORMER
    public @NotBlank String formatDisplay(@NotBlank String prefix) {
        DataValidation.notBlank(prefix);
        return prefix + ": " + name + " (" + price + ")";
    }

    public @NotNull @Positive BigDecimal applyTax(@DecimalMin("0.0") @DecimalMax("1.0") BigDecimal taxRate) {
        DataValidation.notNull(taxRate);
        DataValidation.decimalMin("0.0", taxRate);
        DataValidation.decimalMax("1.0", taxRate);
        return price.multiply(BigDecimal.ONE.add(taxRate));
    }

    // OPERATION
    @Valid
    public ObjectAnnotatedService merge(@NotNull ObjectAnnotatedService other,
                                        @NotBlank String newName) {
        DataValidation.notNull(other);
        DataValidation.notBlank(newName);
        return new ObjectAnnotatedService(newName,
                this.price.add(other.price),
                LocalDate.now(),
                this.email);
    }

    public void copyFrom(@NotNull ObjectAnnotatedService source,
                         @NotBlank String reason) {
        DataValidation.notNull(source);
        DataValidation.notBlank(reason);
        this.name  = source.name;
        this.price = source.price;
    }
}
