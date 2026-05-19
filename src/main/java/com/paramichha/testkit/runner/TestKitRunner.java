package com.paramichha.testkit.runner;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.GenerationResult;
import com.paramichha.testkit.TestKitsX;
import com.paramichha.testkit.scan.TestableScanner;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Batch runner — generate tests for multiple classes at once.
 *
 * <pre>
 * // From annotations — scans package for @Testable / @Testables
 * TestKitRunner.scanPackage("com.example.requests")
 *              .writeTo(Path.of("src/test/java"));
 *
 * // Explicit classes
 * TestKitRunner.forClasses(UserRequest.class, PaymentRequest.class)
 *              .writeTo(Path.of("src/test/java"));
 * </pre>
 */
public final class TestKitRunner {

    private final List<TestableScanner.ScanResult> targets;

    private TestKitRunner(List<TestableScanner.ScanResult> targets) {
        this.targets = targets;
    }

    /** Scan a package for {@code @Testable} and {@code @Testables} annotations. */
    public static TestKitRunner scanPackage(String packageName) {
        return new TestKitRunner(TestableScanner.scan(packageName));
    }

    /** Explicitly provide classes — uses each class's own package as target. */
    public static TestKitRunner forClasses(Class<?>... classes) {
        List<TestableScanner.ScanResult> results = Arrays.stream(classes)
                .map(c -> new TestableScanner.ScanResult(c, c.getPackageName()))
                .toList();
        return new TestKitRunner(results);
    }

    /** Generate and write all test classes to {@code sourceRoot}. */
    public List<Path> writeTo(Path sourceRoot) {
        return targets.stream()
                .map(t -> TestKitsX.of(t.type())
                        .withPackage(t.targetPackage())
                        .generateTest()
                        .writeTo(sourceRoot))
                .toList();
    }

    /** Generate all test classes and return as strings — useful for preview. */
    public List<GenerationResult> generate() {
        return targets.stream()
                .map(t -> TestKitsX.of(t.type())
                        .withPackage(t.targetPackage())
                        .generateTest())
                .toList();
    }

    public int targetCount() { return targets.size(); }

    /**
     * Generate and write both PRODUCTION and DEV mode test classes.
     * Each class produces two files:
     *   ClassNameTest.java     — PRODUCTION (realistic, non-null, positive)
     *   ClassNameDevTest.java  — DEV (full type range, nullable)
     *
     * Both files are written to {@code sourceRoot} and both should pass when run.
     */
    public List<Path> writeBothModesTo(Path sourceRoot) {
        return targets.stream()
                .flatMap(t -> java.util.stream.Stream.of(
                        TestKitsX.of(t.type()).withPackage(t.targetPackage())
                               .withMode(GenerationMode.PRODUCTION)
                               .generateTest().writeTo(sourceRoot),
                        TestKitsX.of(t.type()).withPackage(t.targetPackage())
                               .withMode(GenerationMode.DEV)
                               .generateTest().writeTo(sourceRoot)))
                .toList();
    }

    /**
     * Generate both PRODUCTION and DEV mode test classes as strings.
     */
    public List<GenerationResult> generateBothModes() {
        return targets.stream()
                .flatMap(t -> java.util.stream.Stream.of(
                        TestKitsX.of(t.type()).withPackage(t.targetPackage())
                               .withMode(GenerationMode.PRODUCTION).generateTest(),
                        TestKitsX.of(t.type()).withPackage(t.targetPackage())
                               .withMode(GenerationMode.DEV).generateTest()))
                .toList();
    }
}
