package com.paramichha.testkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Output of {@link TestKit#generateTest()} — the generated Java source.
 *
 * <p>On write: unused imports are removed, then source is formatted
 * using Google Java Format — clean, consistent output every time.
 * Falls back to raw source if JVM flags are missing.
 */
public record GenerationResult(
        String className,
        String packageName,
        String source
) {
    /**
     * Writes the generated class to {@code sourceRoot}.
     * Skips writing if the file already exists with identical content —
     * no diff in version control when regenerating unchanged classes.
     */
    public Path writeTo(Path sourceRoot) {
        try {
            String formatted = format(source);
            Path dir = sourceRoot;
            for (String part : packageName.split("\\.")) dir = dir.resolve(part);
            Files.createDirectories(dir);
            Path file = dir.resolve(className + ".java");
            if (Files.exists(file) && Files.readString(file).equals(formatted)) {
                return file;
            }
            Files.writeString(file, formatted);
            return file;
        } catch (IOException e) {
            throw new TestKitException("Failed to write " + className + ".java", e);
        }
    }


    public String asString() {
        return source;
    }

    // ── Formatting ────────────────────────────────────────────────────────

    /**
     * Two-pass cleanup:
     * 1. RemoveUnusedImports — removes any import not referenced in the source
     * 2. Formatter           — formats whitespace, indentation, line breaks
     * Requires JVM flags (add to .mvn/jvm.config):
     * --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
     * --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
     * --add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
     * --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
     * --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
     * Falls back to raw source if flags are missing.
     */
    private static String format(String source) {
        try {
            // Pass 1 — remove unused imports
            String cleaned = com.google.googlejavaformat.java.RemoveUnusedImports
                    .removeUnusedImports(source);
            // Pass 2 — format
            return new com.google.googlejavaformat.java.Formatter().formatSource(cleaned);
        } catch (Exception | IllegalAccessError ignored) {
            return source;
        }
    }
}