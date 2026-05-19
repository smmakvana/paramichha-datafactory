package com.paramichha.testkit.examples;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.GenerationResult;
import com.paramichha.testkit.TestKit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility — generate a test class for any annotated class and write it to disk.
 *
 * <p>Run this directly in your IDE to generate test files on demand.
 * Change the TARGET_CLASS and OUTPUT_DIR to match your project.
 *
 * <pre>
 * // To generate for your own class:
 * //   1. Change TARGET_CLASS to your annotated class
 * //   2. Change OUTPUT_DIR to your test source root
 * //   3. Right-click → Run
 * </pre>
 */
public class GenerateTestForClass {

    /**
     * The class to generate tests for.
     * Replace with your own annotated request/command/DTO class.
     */
    private static final Class<?> TARGET_CLASS = ExampleRequest.class;

    /**
     * Where to write the generated test file.
     * Change to your project's test source root.
     */
    private static final Path OUTPUT_DIR = Path.of("src/test/java");

    public static void main(String[] args) throws IOException {

        System.out.println("Generating test for: " + TARGET_CLASS.getName());
        System.out.println("Output directory:    " + OUTPUT_DIR.toAbsolutePath());
        System.out.println();

        GenerationResult generated = TestKit.of(TARGET_CLASS)
                .withMode(GenerationMode.PRODUCTION)
                .generateTest();

        Path outputFile = generated.writeTo(OUTPUT_DIR);

        System.out.println("Generated: " + outputFile);
        System.out.println();
        System.out.println("=== Preview (first 40 lines) ===");
        Files.readAllLines(outputFile).stream()
                .limit(40)
                .forEach(System.out::println);
        System.out.println("...");
    }
}
