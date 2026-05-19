package com.paramichha.testkit.examples;


import com.paramichha.testkit.TestKit;

import java.nio.file.Path;

/**
 * Quick test generation runner.
 * <p>
 * Add any class to generate() and run main().
 * Output goes to src/test/java by default — change OUTPUT_DIR if needed.
 */
public class TestKitApp {

    static final Path OUTPUT_DIR = Path.of("src/test/java");

    public static void main(String[] args) {
        generate(
                PrimitiveAnnotatedService.class,  // primitives with annotations
                PrimitivePlainService.class,       // primitives without annotations
                PrimitiveMixedService.class,       // primitives mixed

                // Object services
                ObjectAnnotatedService.class,      // objects with annotations
                ObjectPlainService.class,          // objects without annotations
                ObjectMixedService.class,          // objects mixed

                // Original sample
                SampleService.class
        );
    }

    static void generate(Class<?>... classes) {
        for (Class<?> type : classes) {
            try {
                Path written = TestKit.of(type).generateTest().writeTo(OUTPUT_DIR);
                System.out.println("✓ " + type.getSimpleName() + " → " + written);
            } catch (Exception e) {
                System.err.println("✗ " + type.getSimpleName() + " → " + e.getMessage());
            }
        }
    }
}