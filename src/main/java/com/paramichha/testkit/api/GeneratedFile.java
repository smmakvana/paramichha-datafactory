package com.paramichha.testkit.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A generated source file — path and content.
 */
public interface GeneratedFile {

    Path path();

    String content();

    default Path writeTo(Path sourceRoot) {
        try {
            Path file = sourceRoot.resolve(path());
            Files.createDirectories(file.getParent());
            if (Files.exists(file) && Files.readString(file).equals(content())) return file;
            Files.writeString(file, content());
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + path(), e);
        }
    }
}