package com.paramichha.testkit.scan;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Scans a package for classes annotated with {@link Testable} or {@link Testables}.
 * Uses standard Java NIO — no classpath scanning libraries required.
 */
public final class TestableScanner {

    public  TestableScanner() {}

    /**
     * Returns all classes in the given package (and subpackages)
     * that carry {@code @Testable}, plus all classes listed in {@code @Testables}.
     */
    public static List<ScanResult> scan(String packageName) {
        List<ScanResult> results = new ArrayList<>();

        for (Class<?> cls : findClasses(packageName)) {
            Testable single = cls.getAnnotation(Testable.class);
            if (single != null) {
                String pkg = single.targetPackage().isBlank()
                        ? cls.getPackageName() : single.targetPackage();
                results.add(new ScanResult(cls, pkg));
            }

            Testables batch = cls.getAnnotation(Testables.class);
            if (batch != null) {
                for (Class<?> target : batch.value()) {
                    String pkg = batch.targetPackage().isBlank()
                            ? target.getPackageName() : batch.targetPackage();
                    results.add(new ScanResult(target, pkg));
                }
            }
        }

        return List.copyOf(results);
    }

    /**
     * One resolved target: the class to generate a test for and where to put it.
     */
    public record ScanResult(Class<?> type, String targetPackage) {}

    // ── classpath scanning ────────────────────────────────────────────────

    private static List<Class<?>> findClasses(String packageName) {
        String path = packageName.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        List<Class<?>> found = new ArrayList<>();

        try {
            Enumeration<java.net.URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URI uri = resources.nextElement().toURI();
                if (uri.getScheme().equals("jar")) {
                    scanJar(uri, path, packageName, cl, found);
                } else {
                    scanDirectory(Path.of(uri), packageName, cl, found);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new com.paramichha.testkit.TestKitException(
                    "Failed to scan package: " + packageName, e);
        }

        return found;
    }

    private static void scanDirectory(Path dir, String packageName,
                                       ClassLoader cl, List<Class<?>> found) {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.filter(p -> p.toString().endsWith(".class"))
                .forEach(p -> {
                    String relative = dir.relativize(p).toString()
                            .replace(FileSystems.getDefault().getSeparator(), ".")
                            .replace(".class", "");
                    loadQuietly(packageName + "." + relative, cl, found);
                });
        } catch (IOException e) {
            throw new com.paramichha.testkit.TestKitException(
                    "Failed to scan directory: " + dir, e);
        }
    }

    private static void scanJar(URI jarUri, String path, String packageName,
                                  ClassLoader cl, List<Class<?>> found) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(jarUri, Map.of())) {
            try (Stream<Path> walk = Files.walk(fs.getPath(path))) {
                walk.filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> {
                        String className = p.toString()
                                .replace("/", ".").substring(1).replace(".class", "");
                        loadQuietly(className, cl, found);
                    });
            }
        }
    }

    private static void loadQuietly(String className, ClassLoader cl, List<Class<?>> found) {
        try { found.add(cl.loadClass(className)); }
        catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
    }
}
