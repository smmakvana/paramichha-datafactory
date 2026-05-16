package com.paramichha.testkit;

import java.io.IOException;
import java.nio.file.*;

/**
 * Output of {@link TestKit#generate()} — the generated Java source.
 *
 * <p>Source is automatically formatted using Google Java Format before writing,
 * producing clean, consistent, import-organised output every time.
 * Falls back to raw source if formatting fails (e.g. syntax error in generated code).
 */
public record GeneratedTestClass(
        String className,
        String packageName,
        String source
) {
    /**
     * Writes the generated class to {@code sourceRoot}, formatted and clean.
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

    /** Returns the formatted source — useful for preview/testing without writing. */
    public String formatted() {
        return format(source);
    }

    public String asString() { return source; }

    // ── Formatting ────────────────────────────────────────────────────────

    /**
     * Formats source using Google Java Format.
     * Falls back to raw source if formatting fails — better to write
     * unformatted than to silently discard output.
     */
    private static String format(String source) {
        try {
            return new com.google.googlejavaformat.java.Formatter().formatSource(source);
        } catch (Exception | IllegalAccessError ignored) {
            // IllegalAccessError means JVM --add-exports flags are not set.
            // Add to your run config or pom surefire:
            //   --add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
            //   --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
            //   --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
            //   --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
            //   --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
            return source;
        }
    }
}