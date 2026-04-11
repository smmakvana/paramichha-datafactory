package com.paramichha.datafactory.examples;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fintech Example — realistic financial test data.
 *
 * <p>DataFactory knows about IBANs, BICs, sort codes, credit cards and account numbers.
 */
public class FintechExample {

    @Value @Builder
    static class PaymentRequest {
        @NotBlank String fromIban;
        @NotBlank String toIban;
        @NotNull @Positive BigDecimal amount;
        @NotBlank String reference;
    }

    @Value @Builder
    static class AccountRequest {
        @NotBlank @Email String email;
        @NotBlank        String fullName;
        @NotBlank        String sortCode;
        @NotBlank        String accountNumber;
        @NotBlank        String bic;
        @NotNull @Positive BigDecimal balance;
    }

    public static void main(String[] args) {
        payments();
        accounts();
        financialStrings();
        bulk();
    }

    static void payments() {
        section("Payment with realistic IBAN data");
        PaymentRequest p = DataFactory.of(PaymentRequest.class)
                .with("fromIban", DataFactory.string().iban().valid())
                .with("toIban",   DataFactory.string().iban().valid())
                .valid();
        System.out.println("  from:      " + p.getFromIban());
        System.out.println("  to:        " + p.getToIban());
        System.out.println("  amount:    " + p.getAmount());
        System.out.println("  reference: " + p.getReference());
    }

    static void accounts() {
        section("UK bank account");
        AccountRequest a = DataFactory.of(AccountRequest.class)
                .with("sortCode",      DataFactory.string().sortCode().valid())
                .with("accountNumber", DataFactory.string().accountNumber().valid())
                .with("bic",           DataFactory.string().bic().valid())
                .valid();
        System.out.println("  email:   " + a.getEmail());
        System.out.println("  sort:    " + a.getSortCode());
        System.out.println("  account: " + a.getAccountNumber());
        System.out.println("  bic:     " + a.getBic());
        System.out.println("  balance: " + a.getBalance());
    }

    static void financialStrings() {
        section("Financial string builders");
        System.out.println("  iban:    " + DataFactory.string().iban().valid());
        System.out.println("  bic:     " + DataFactory.string().bic().valid());
        System.out.println("  card:    " + DataFactory.string().creditCard().valid());
        System.out.println("  sort:    " + DataFactory.string().sortCode().valid());
        System.out.println("  account: " + DataFactory.string().accountNumber().valid());
    }

    static void bulk() {
        section("100 payments for load testing");
        List<PaymentRequest> payments = DataFactory.of(PaymentRequest.class).stream(100);
        long unique = payments.stream().map(PaymentRequest::getFromIban).distinct().count();
        System.out.println("  generated:    " + payments.size());
        System.out.println("  unique ibans: " + unique);
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }
}
