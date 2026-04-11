package com.paramichha.datafactory.fixture.annotation;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.URL;
public class StringFormatPojo {
    @NotBlank @Email String email;
    @NotBlank @URL String website;
    @NotBlank @Pattern(regexp="[A-Z]{2}\\d{4}") String reference;
    @NotBlank @CreditCardNumber String cardNumber;
    @NotBlank @EAN String barcode;
    @NotBlank @ISBN String bookCode;
    public StringFormatPojo() {}
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
}
