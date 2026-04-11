package com.paramichha.datafactory.examples;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Object Generation — DataFactory reads your Jakarta annotations and generates
 * data that satisfies them automatically. No setup. No hardcoded strings.
 */
public class ObjectGeneration {

    @Value @Builder
    static class CustomerRequest {
        @NotBlank @Email @Size(max = 100) String email;
        @NotBlank @Size(min = 2, max = 50) String fullName;
        @NotNull @Min(18) @Max(120)        Integer age;
        @NotNull @Past                     LocalDate dateOfBirth;
        @NotNull @Positive                 BigDecimal accountBalance;
    }

    public static void main(String[] args) {
        validObject();
        fieldOverrides();
        nullField();
        allViolations();
        invalidForField();
        boundaryVariants();
        bulkGeneration();
        deterministicSeed();
    }

    static void validObject() {
        section("One valid object");
        CustomerRequest r = DataFactory.of(CustomerRequest.class).valid();
        System.out.println("  email:   " + r.getEmail());
        System.out.println("  name:    " + r.getFullName());
        System.out.println("  age:     " + r.getAge());
        System.out.println("  dob:     " + r.getDateOfBirth());
        System.out.println("  balance: " + r.getAccountBalance());
    }

    static void fieldOverrides() {
        section("Override specific fields — everything else stays valid");
        CustomerRequest r = DataFactory.of(CustomerRequest.class)
                .with("email", "fixed@mycompany.com")
                .with("age", 25)
                .valid();
        System.out.println("  email: " + r.getEmail() + "  age: " + r.getAge());
        System.out.println("  name still generated: " + r.getFullName());
    }

    static void nullField() {
        section("Null a specific field");
        CustomerRequest r = DataFactory.of(CustomerRequest.class)
                .withNull("dateOfBirth")
                .valid();
        System.out.println("  dob:     " + r.getDateOfBirth() + "  (null)");
        System.out.println("  balance: " + r.getAccountBalance() + "  (still generated)");
    }

    static void allViolations() {
        section("All constraint violations — one object per constraint");
        List<CustomerRequest> bad = DataFactory.of(CustomerRequest.class).invalidList();
        System.out.println("  " + bad.size() + " invalid customers:");
        bad.forEach(c -> System.out.println(
                "    email=" + shorten(c.getEmail(), 22)
                + "  age=" + c.getAge()));
    }

    static void invalidForField() {
        section("Invalid for one specific field");
        CustomerRequest r = DataFactory.of(CustomerRequest.class).invalidFor("email");
        System.out.println("  email:   " + r.getEmail() + "  ← invalid");
        System.out.println("  name:    " + r.getFullName() + "  ← valid");
        System.out.println("  age:     " + r.getAge() + "  ← valid");
    }

    static void boundaryVariants() {
        section("Boundary variants per field");
        var map = DataFactory.of(CustomerRequest.class).validMap();
        map.forEach((field, variants) -> {
            if (field.equals("age")) {
                System.out.println("  age boundaries: "
                        + variants.stream().map(c -> String.valueOf(c.getAge())).toList());
            }
        });
    }

    static void bulkGeneration() {
        section("1000 customers in one line");
        List<CustomerRequest> bulk = DataFactory.of(CustomerRequest.class).stream(1000);
        long unique = bulk.stream().map(CustomerRequest::getEmail).distinct().count();
        System.out.println("  generated:    " + bulk.size());
        System.out.println("  unique emails: " + unique);
    }

    static void deterministicSeed() {
        section("Deterministic — same seed, same data");
        CustomerRequest a = DataFactory.of(CustomerRequest.class).seed(42L).valid();
        CustomerRequest b = DataFactory.of(CustomerRequest.class).seed(42L).valid();
        System.out.println("  run 1: " + a.getEmail());
        System.out.println("  run 2: " + b.getEmail());
        System.out.println("  same:  " + a.getEmail().equals(b.getEmail()));
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }

    static String shorten(Object v, int max) {
        String s = v == null ? "null" : v.toString();
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
