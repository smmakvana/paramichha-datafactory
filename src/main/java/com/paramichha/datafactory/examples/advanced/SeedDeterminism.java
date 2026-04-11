package com.paramichha.datafactory.examples.advanced;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

/**
 * Seed Determinism — same seed, same data, every time.
 *
 * <p>Use seed() for snapshot tests, regression tests, and documented examples in code reviews.
 */
public class SeedDeterminism {

    @Value @Builder
    static class UserRequest {
        @NotBlank @Email String email;
        @NotBlank        String name;
        @NotNull @Min(18) @Max(120) Integer age;
    }

    public static void main(String[] args) {
        withoutSeed();
        withSeed();
        differentSeeds();
    }

    static void withoutSeed() {
        section("Without seed — different values every run");
        System.out.println("  " + DataFactory.of(UserRequest.class).valid().getEmail());
        System.out.println("  " + DataFactory.of(UserRequest.class).valid().getEmail());
        System.out.println("  " + DataFactory.of(UserRequest.class).valid().getEmail());
        System.out.println("  (all different)");
    }

    static void withSeed() {
        section("With seed(42) — identical values every run");
        UserRequest a = DataFactory.of(UserRequest.class).seed(42L).valid();
        UserRequest b = DataFactory.of(UserRequest.class).seed(42L).valid();
        System.out.println("  run 1: " + a.getEmail() + " / " + a.getName() + " / " + a.getAge());
        System.out.println("  run 2: " + b.getEmail() + " / " + b.getName() + " / " + b.getAge());
        System.out.println("  same:  " + a.getEmail().equals(b.getEmail()));
    }

    static void differentSeeds() {
        section("Different seeds — different data");
        System.out.println("  seed 1: " + DataFactory.of(UserRequest.class).seed(1L).valid().getEmail());
        System.out.println("  seed 2: " + DataFactory.of(UserRequest.class).seed(2L).valid().getEmail());
        System.out.println("  seed 3: " + DataFactory.of(UserRequest.class).seed(3L).valid().getEmail());
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }
}
