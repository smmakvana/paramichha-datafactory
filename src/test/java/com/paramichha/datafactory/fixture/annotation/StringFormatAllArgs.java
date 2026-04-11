package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class StringFormatAllArgs {
    @NotBlank @Email String email;
    @NotBlank @URL String website;
    @NotBlank @Pattern(regexp="[A-Z]{2}\\d{4}") String reference;
    @NotBlank @CreditCardNumber String cardNumber;
    @NotBlank @EAN String barcode;
    @NotBlank @ISBN String bookCode;
}
