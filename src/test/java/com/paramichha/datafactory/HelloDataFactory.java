package com.paramichha.datafactory;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A guided tour of DataFactory — runs as a standalone main method.
 *
 * <p>Run this class directly to see what DataFactory generates with your own eyes.
 * No test framework, no assertions — just output showing the three levels of the API.
 */
public class HelloDataFactory {

    @Value
    @Builder
    static class CustomerRequest {
        @NotBlank @Email @Size(max = 100) String email;
        @NotBlank @Size(min = 2, max = 50) String name;
        @NotNull @Min(18) @Max(120) Integer age;
        @NotNull @Past LocalDate dateOfBirth;
        @NotNull @Positive BigDecimal accountBalance;
    }

    @Value
    @Builder
    static class PaymentRequest {
        @NotBlank String fromIban;
        @NotBlank String toIban;
        @NotNull @Positive BigDecimal amount;
        @NotBlank String reference;
    }

    enum OrderStatus { PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED }

    public static void main(String[] args) {
        levelOne();
        levelTwo();
        levelThree();
    }

    private static void levelOne() {
        title("Level 1 — instant values");
        System.out.println("  email:    " + DataFactory.email());
        System.out.println("  name:     " + DataFactory.name());
        System.out.println("  phone:    " + DataFactory.phone());
        System.out.println("  company:  " + DataFactory.company());
        System.out.println("  city:     " + DataFactory.city());
        System.out.println("  postcode: " + DataFactory.postcode());
        System.out.println("  uuid:     " + DataFactory.uuid());
        System.out.println("  5 emails: " + DataFactory.emails(5));
    }

    private static void levelTwo() {
        title("Level 2 — typed builders");
        System.out.println("  string email:  " + DataFactory.string().email().valid());
        System.out.println("  string iban:   " + DataFactory.string().iban().valid());
        System.out.println("  string card:   " + DataFactory.string().creditCard().valid());
        System.out.println("  string bic:    " + DataFactory.string().bic().valid());
        System.out.println("  integer:       " + DataFactory.integer().range(18, 65).valid());
        System.out.println("  long:          " + DataFactory.longVal().positive().toLong());
        System.out.println("  decimal:       " + DataFactory.decimal().positive().scale(2).valid());
        System.out.println("  date past:     " + DataFactory.date().past().valid());
        System.out.println("  datetime fut:  " + DataFactory.dateTime().future().valid());
        System.out.println("  enum:          " + DataFactory.enumOf(OrderStatus.class));
        System.out.println("  5 integers:    " + DataFactory.integer().range(18, 65).stream(5));
    }

    private static void levelThree() {
        title("Level 3 — object builders");

        CustomerRequest valid = DataFactory.of(CustomerRequest.class).valid();
        System.out.println("  valid customer:");
        System.out.println("    email:   " + valid.getEmail());
        System.out.println("    name:    " + valid.getName());
        System.out.println("    age:     " + valid.getAge());
        System.out.println("    dob:     " + valid.getDateOfBirth());
        System.out.println("    balance: " + valid.getAccountBalance());

        CustomerRequest pinned = DataFactory.of(CustomerRequest.class)
                .with("email", "fixed@company.com")
                .with("age", 25)
                .valid();
        System.out.println();
        System.out.println("  with overrides — email: " + pinned.getEmail()
                + ", age: " + pinned.getAge());

        List<CustomerRequest> bad = DataFactory.of(CustomerRequest.class).invalidList();
        System.out.println();
        System.out.println("  " + bad.size() + " invalid customers — one per constraint:");
        bad.forEach(c -> System.out.println("    email=" + c.getEmail()
                + " age=" + c.getAge()));

        CustomerRequest badEmail = DataFactory.of(CustomerRequest.class).invalidFor("email");
        System.out.println();
        System.out.println("  invalidFor(email) — email: " + badEmail.getEmail()
                + "  (everything else valid)");

        var edges = DataFactory.of(CustomerRequest.class).validMap();
        System.out.println();
        System.out.println("  age boundary values: "
                + edges.get("age").stream().map(CustomerRequest::getAge).toList());

        List<CustomerRequest> bulk = DataFactory.of(CustomerRequest.class).stream(1000);
        System.out.println();
        System.out.println("  generated " + bulk.size() + " customers");

        CustomerRequest a = DataFactory.of(CustomerRequest.class).seed(42L).valid();
        CustomerRequest b = DataFactory.of(CustomerRequest.class).seed(42L).valid();
        System.out.println();
        System.out.println("  seed 42: " + a.getEmail());
        System.out.println("  same:    " + b.getEmail());
        System.out.println("  stable:  " + a.getEmail().equals(b.getEmail()));

        title("Real-world: payment with realistic data");
        PaymentRequest payment = DataFactory.of(PaymentRequest.class)
                .with("fromIban", DataFactory.string().iban().valid())
                .with("toIban",   DataFactory.string().iban().valid())
                .valid();
        System.out.println("  from:   " + payment.getFromIban());
        System.out.println("  to:     " + payment.getToIban());
        System.out.println("  amount: " + payment.getAmount());
        System.out.println("  ref:    " + payment.getReference());

        System.out.println();
        System.out.println("  No boilerplate. No hardcoded strings. No maintenance.");
    }

    private static void title(String text) {
        System.out.println();
        System.out.println(text);
        System.out.println("  " + "-".repeat(text.length()));
    }
}
