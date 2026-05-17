package com.paramichha.testkit.examples;

import com.paramichha.utils.DataValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Covers every method kind + return type + annotation permutation
 * that TestKit needs to handle. Used to verify generated tests.
 */
public class SampleService {

    @NotBlank
    private String name;

    @Min(1) @Max(100)
    private int quantity;

    @NotNull @Positive
    private BigDecimal price;

    public SampleService(@NotBlank String name,
                         @Min(1) @Max(100) int quantity,
                         @NotNull @Positive BigDecimal price) {
        this.name     = name;
        this.quantity = quantity;
        this.price    = price;
    }

    // =========================================================================
    // COMMAND — void, no params
    // =========================================================================

    /** COMMAND — no params, class has annotations */
    public void reset() {
        this.quantity = 1;
        this.price    = BigDecimal.ONE;
    }

    // =========================================================================
    // QUERY — no params, all return type + annotation permutations
    // =========================================================================

    /** QUERY — primitive, annotated */
    @Min(1) @Max(100)
    public int getQuantity() { return quantity; }

    /** QUERY — primitive, no annotation */
    public int getRawCount() { return quantity * 2; }

    /** QUERY — boolean primitive, no annotation */
    public boolean isActive() { return quantity > 0; }

    /** QUERY — String, annotated */
    @NotBlank
    public String getName() { return name; }

    /** QUERY — String, no annotation */
    public String getDisplayName() { return "Item: " + name; }

    /** QUERY — Object with constrained fields */
    @NotNull
    public BigDecimal getPrice() { return price; }

    /** QUERY — plain Object, no annotation */
    public Object getMetadata() { return null; }

    // =========================================================================
    // MUTATOR — void, one param, annotation permutations
    // =========================================================================

    /** MUTATOR — constrained String param */
    public void setName(@NotBlank String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        this.name = name;
    }

    /** MUTATOR — constrained primitive param */
    public void setQuantity(@Min(1) @Max(100) int quantity) { this.quantity = quantity; }

    /** MUTATOR — unannotated param */
    public void setMetadata(String metadata) { /* no-op */ }

    // =========================================================================
    // TRANSFORMER — one param, returns value, all permutations
    // =========================================================================

    /** TRANSFORMER — annotated param, annotated Object return */
    @NotNull @Positive
    public BigDecimal applyDiscount(@DecimalMin("0.0") @DecimalMax("1.0") BigDecimal rate) {
        DataValidation.notNull(rate, "rate must not be null");
        DataValidation.decimalMin("0.0", rate);
        DataValidation.decimalMax("1.0", rate);
        return price.multiply(BigDecimal.ONE.subtract(rate));
    }

    /** TRANSFORMER — annotated param, unannotated String return */
    public String formatName(@NotBlank String prefix) {
        if (prefix == null || prefix.isBlank()) throw new IllegalArgumentException("prefix must not be blank");
        return prefix + " " + name;
    }

    /** TRANSFORMER — unannotated param, annotated return */
    @NotBlank
    public String describe(String separator) {
        return name + separator + quantity;
    }

    /** TRANSFORMER — unannotated param, unannotated return */
    public String encode(String format) {
        return format + ":" + name;
    }

    /** TRANSFORMER — unannotated param, primitive return */
    public int multiply(int factor) {
        return quantity * factor;
    }

    // =========================================================================
    // OPERATION — multiple params, all permutations
    // =========================================================================

    /** OPERATION — all params constrained, returns annotated Object */
    @Valid
    public SampleService merge(@NotNull SampleService other,
                               @NotBlank String newName) {
        DataValidation.notNull(other, "other must not be null");
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("newName must not be blank");
        return new SampleService(newName,
                this.quantity + other.quantity,
                this.price.add(other.price));
    }

    /** OPERATION — some params constrained, void return */
    public void transferTo(@NotNull SampleService target, int amount) {
        DataValidation.notNull(target, "target must not be null");
        target.setQuantity(target.getQuantity() + amount);
    }

    /** OPERATION — no params constrained, returns primitive */
    public int combine(int a, int b) {
        return a + b;
    }

    /** OPERATION — no params constrained, void return */
    public void swap(String a, String b) { /* no-op */ }
}