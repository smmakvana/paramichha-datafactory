package com.paramichha.datafactory.examples.advanced;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.GenerationMode;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

/**
 * Generation Modes — PRODUCTION (default) vs DEV.
 *
 * <p>PRODUCTION: injects safe defaults for unannotated fields.
 * DEV: no injection, full type range for unannotated numerics — reveals overflow bugs.
 */
public class GenerationModes {

    @Value @Builder
    static class PartialRequest {
        @NotBlank String email;
        Integer count;
        String label;
    }

    public static void main(String[] args) {
        productionMode();
        devMode();
        devBoundaries();
    }

    static void productionMode() {
        section("PRODUCTION mode — safe defaults injected for unannotated fields");
        var r = DataFactory.of(PartialRequest.class).mode(GenerationMode.PRODUCTION).valid();
        System.out.println("  email: " + r.getEmail() + "  count: " + r.getCount()
                + "  label: " + r.getLabel());
        System.out.println("  all non-null, count is positive");
    }

    static void devMode() {
        section("DEV mode — annotations as-is, no injection");
        var r = DataFactory.of(PartialRequest.class).mode(GenerationMode.DEV).valid();
        System.out.println("  email: " + r.getEmail() + "  count: " + r.getCount()
                + "  label: " + r.getLabel());
        System.out.println("  count and label may be null");
    }

    static void devBoundaries() {
        section("DEV mode — validMap() exposes type extremes for unannotated Integer");
        var map = DataFactory.of(PartialRequest.class).mode(GenerationMode.DEV).validMap();
        if (map.containsKey("count")) {
            var counts = map.get("count").stream().map(PartialRequest::getCount).toList();
            System.out.println("  count boundaries: " + counts);
            System.out.println("  includes MIN_VALUE and MAX_VALUE");
            System.out.println("  → catches column overflow before production");
        }
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }
}
