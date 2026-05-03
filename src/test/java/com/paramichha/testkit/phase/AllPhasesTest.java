package com.paramichha.testkit.phase;

import com.paramichha.testkit.runner.TestKitRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Generates all fixture test classes into src/test/java.
 *
 * <p>This is a generation runner, not a test suite. Its only job is to invoke
 * TestKit for every {@code @Testable} fixture and write the generated files.
 * If generation fails for any class, TestKit writes a placeholder with the
 * error — that is the signal to fix DataFactory or the fixture, not this class.
 *
 * <p>Generates two files per fixture:
 * <ul>
 *   <li>{@code ClassNameTest.java} — PRODUCTION mode</li>
 *   <li>{@code ClassNameDevTest.java} — DEV mode</li>
 * </ul>
 *
 * <p>Generation is idempotent. Running this twice produces no file changes
 * as long as fixtures have not changed.
 */
@DisplayName("All Phases")
class AllPhasesTest {

    private static final Path TEST_ROOT = Paths.get("src", "test", "java");

    @Test
    @DisplayName("Generate all fixture test classes")
    void generateAll() {
        List<Path> written = TestKitRunner
                .scanPackage("com.paramichha.testkit.fixture")
                .writeBothModesTo(TEST_ROOT);

        long production = written.stream()
                .filter(p -> !p.getFileName().toString().contains("Dev")).count();
        long dev = written.stream()
                .filter(p -> p.getFileName().toString().contains("Dev")).count();

        System.out.println();
        System.out.println("TestKit — generation complete");
        System.out.println("  Total:      " + written.size() + " files");
        System.out.println("  Production: " + production);
        System.out.println("  Dev:        " + dev);
        System.out.println("  Output:     " + TEST_ROOT.toAbsolutePath());
    }
}