package com.paramichha.datafactory.examples;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Getting Started — five minutes to understand DataFactory.
 *
 * <p>Three levels. Run this and you will know everything you need for 90% of your test data needs.
 * To run: right-click → Run in your IDE.
 */
public class GettingStarted {

    @Value @Builder
    static class UserRequest {
        @NotBlank @Email @Size(max = 100) String email;
        @NotBlank @Size(min = 2, max = 50) String name;
        @NotNull @Min(18) @Max(120)        Integer age;
    }

    enum Status { ACTIVE, INACTIVE, PENDING }

    public static void main(String[] args) {
        levelOne();
        levelTwo();
        levelThree();
    }

    static void levelOne() {
        section("Level 1 — instant values, zero config");
        System.out.println("  email:    " + DataFactory.email());
        System.out.println("  name:     " + DataFactory.name());
        System.out.println("  phone:    " + DataFactory.phone());
        System.out.println("  company:  " + DataFactory.company());
        System.out.println("  uuid:     " + DataFactory.uuid());
        System.out.println("  5 emails: " + DataFactory.emails(5));
    }

    static void levelTwo() {
        section("Level 2 — typed builders");
        System.out.println("  string email:  " + DataFactory.string().email().valid());
        System.out.println("  string iban:   " + DataFactory.string().iban().valid());
        System.out.println("  integer range: " + DataFactory.integer().range(18, 65).valid());
        System.out.println("  decimal pos:   " + DataFactory.decimal().positive().scale(2).valid());
        System.out.println("  date past:     " + DataFactory.date().past().valid());
        System.out.println("  enum:          " + DataFactory.enumOf(Status.class));
        System.out.println("  5 ages:        " + DataFactory.integer().range(18, 65).stream(5));
    }

    static void levelThree() {
        section("Level 3 — object builders, reads your annotations");
        UserRequest valid = DataFactory.of(UserRequest.class).valid();
        System.out.println("  email: " + valid.getEmail());
        System.out.println("  name:  " + valid.getName());
        System.out.println("  age:   " + valid.getAge());

        List<UserRequest> bad = DataFactory.of(UserRequest.class).invalidList();
        System.out.println();
        System.out.println("  " + bad.size() + " invalid users — one per constraint:");
        bad.forEach(u -> System.out.println(
                "    email=" + shorten(u.getEmail(), 20)
                + "  name=" + shorten(u.getName(), 15)
                + "  age=" + u.getAge()));
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
