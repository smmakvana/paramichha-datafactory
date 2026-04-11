package com.paramichha.datafactory.snapshot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads and writes snapshot JSON files.
 *
 * Files live in: src/test/resources/snapshots/FixtureName.json
 *
 * Two modes:
 *   generate mode  (-Dsnapshot.generate=true) → writes files, always passes
 *   compare mode   (default)                  → compares files, fails if missing or different
 */
public final class SnapshotStore {

    static final String SNAPSHOT_DIR = "snapshots";
    static final String GENERATE_FLAG = "snapshot.generate";

    private SnapshotStore() {}

    public static boolean isGenerateMode() {
        return Boolean.getBoolean(GENERATE_FLAG);
    }

    public static boolean exists(String fixtureName) {
        try {
            return resolveReadPath(fixtureName) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static String read(String fixtureName) {
        try {
            Path path = resolveReadPath(fixtureName);
            if (path == null) return null;
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read snapshot: " + fixtureName, e);
        }
    }

    public static void write(String fixtureName, String json) {
        try {
            Path dir = resolveWriteDir();
            Files.createDirectories(dir);
            Path file = dir.resolve(fixtureName + ".json");
            Files.writeString(file, json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write snapshot: " + fixtureName, e);
        }
    }

    private static Path resolveReadPath(String fixtureName) {
        String resource = SNAPSHOT_DIR + "/" + fixtureName + ".json";
        URL url = SnapshotStore.class.getClassLoader().getResource(resource);
        if (url == null) return null;
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static Path resolveWriteDir() {
        try {
            // Strategy 1: find an existing snapshot and write next to it
            // Most reliable — navigates from a file we know exists
            URL existing = SnapshotStore.class.getClassLoader()
                    .getResource(SNAPSHOT_DIR + "/");
            if (existing != null) {
                Path dir = Paths.get(existing.toURI());
                // This is target/test-classes/snapshots/ — walk up to find src/
                Path p = dir;
                while (p != null) {
                    Path candidate = p.resolve("src/test/resources/" + SNAPSHOT_DIR);
                    if (candidate.toFile().exists()) {
                        System.out.println("  [SnapshotStore] Writing to: " + candidate);
                        return candidate;
                    }
                    p = p.getParent();
                }
            }
            // Strategy 2: navigate from code source location
            Path codeSource = Paths.get(
                    SnapshotStore.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI());
            // codeSource = target/test-classes
            // go up to project root and find src/test/resources/snapshots
            Path p = codeSource;
            while (p != null) {
                Path candidate = p.resolve("src/test/resources/" + SNAPSHOT_DIR);
                if (candidate.getParent().toFile().exists()) {
                    java.nio.file.Files.createDirectories(candidate);
                    System.out.println("  [SnapshotStore] Writing to: " + candidate);
                    return candidate;
        }
                p = p.getParent();
            }
            throw new RuntimeException("Cannot find src/test/resources directory");
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve snapshot write path: " + e.getMessage(), e);
    }
}
}