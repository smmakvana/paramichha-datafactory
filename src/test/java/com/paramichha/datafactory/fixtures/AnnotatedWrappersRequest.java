package com.paramichha.datafactory.fixtures;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

/**
 * Fixture 5 — wrapper types with annotations.
 * Exercises: @NotNull, @Min, @Max, @Email, @Size, @URL, @CreditCardNumber,
 *
 * @Digits, @Positive, @Negative, @NegativeOrZero on wrapper types.
 * Drives all annotation-driven ConstraintCase branches.
 */
@Value
@Builder
public class AnnotatedWrappersRequest {
    @NotNull
    @Min(1)
    @Max(100)
    Byte byteVal;
    @NotNull
    @Min(1)
    @Max(30000)
    Short shortVal;
    @NotNull
    @Min(0)
    @Max(99999)
    Integer intVal;
    @NotNull
    @Positive
    Long longVal;
    @NotNull
    @Min(0)
    @Max(100)
    Float floatVal;
    @NotNull
    @Min(0)
    @Max(100)
    Double doubleVal;
    @NotNull
    Boolean boolVal;
    @NotNull
    @Digits(integer = 8, fraction = 2)
    BigDecimal bigDecimalVal;
    @NotNull
    @Min(0)
    BigInteger bigIntegerVal;
    @NotNull
    UUID uuidVal;
    @NotBlank
    @Email
    @Size(min = 8, max = 100)
    String email;
    @NotBlank
    @Size(min = 2, max = 50)
    String name;
    @NotBlank
    @URL
    String website;
}
