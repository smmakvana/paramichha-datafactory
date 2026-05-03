package com.paramichha.testkit;

import java.io.IOException;
import java.nio.file.*;

/**
 * Output of {@link TestKit#generate()} — the generated Java source.
 */
public record GeneratedTestClass(
        String className,
        String packageName,
        String source
) {
    /**
     * Writes the generated class to {@code sourceRoot}.
     * If the file already exists with identical content, it is not touched —
     * this ensures that running generation twice produces no diff in version control.
     */
    public Path writeTo(Path sourceRoot) {
        try {
            Path dir = sourceRoot;
            for (String part : packageName.split("\\.")) dir = dir.resolve(part);
            Files.createDirectories(dir);
            Path file = dir.resolve(className + ".java");
            if (Files.exists(file) && Files.readString(file).equals(source)) {
                return file;
            }
            Files.writeString(file, source);
            return file;
        } catch (IOException e) {
            throw new TestKitException("Failed to write " + className + ".java", e);
        }
    }

    public String asString() { return source; }
}
