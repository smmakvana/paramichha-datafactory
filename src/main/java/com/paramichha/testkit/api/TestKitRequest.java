package com.paramichha.testkit.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Immutable request describing how a test class should be generated.
 *
 * <p>The target class is mandatory and drives sensible defaults:
 * <ul>
 *     <li>Package name</li>
 *     <li>Test source root</li>
 *     <li>Project configuration</li>
 * </ul>
 *
 * <pre>
 * TestKitRequest request = TestKitRequest.of(UserService.class)
 *     .withOutputDir(Path.of("custom-tests"))
 *     .withProjectConfig(ProjectConfig.detect());
 * </pre>
 */
public final class TestKitRequest {

    private final Class<?> targetClass;
    private final Path outputDir;
    private final ProjectConfig projectConfig;

    private TestKitRequest(
            Class<?> targetClass,
            Path outputDir,
            ProjectConfig projectConfig
    ) {
        this.targetClass = Objects.requireNonNull(targetClass, "targetClass");
        this.outputDir = Objects.requireNonNull(outputDir, "outputDir");
        this.projectConfig = Objects.requireNonNull(projectConfig, "projectConfig");
    }

    /**
     * Creates a request using framework defaults.
     */
    public static TestKitRequest of(Class<?> targetClass) {
        Objects.requireNonNull(targetClass, "targetClass");

        return new TestKitRequest(
                targetClass,
                resolveDefaultOutputDir(targetClass),
                ProjectConfig.detect()
        );
    }

    /**
     * Overrides generated test output directory.
     */
    public TestKitRequest withOutputDir(Path outputDir) {
        return new TestKitRequest(
                targetClass,
                normalize(outputDir),
                projectConfig
        );
    }

    /**
     * Overrides detected project configuration.
     */
    public TestKitRequest withProjectConfig(ProjectConfig projectConfig) {
        return new TestKitRequest(
                targetClass,
                outputDir,
                projectConfig
        );
    }

    public Class<?> targetClass() {
        return targetClass;
    }

    public Path outputDir() {
        return outputDir;
    }

    public ProjectConfig projectConfig() {
        return projectConfig;
    }

    /**
     * Resolves the conventional Maven/Gradle test source root.
     *
     * <pre>
     * com.example.UserService
     * -> src/test/java
     * </pre>
     */
    private static Path resolveDefaultOutputDir(Class<?> type) {
        return Path.of("src", "test", "java");
    }

    /**
     * Normalizes path for stable comparisons and generation.
     */
    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /**
     * Returns true if the output directory already exists.
     */
    public boolean outputDirectoryExists() {
        return Files.exists(outputDir);
    }

    @Override
    public String toString() {
        return "TestKitRequest[" +
                "targetClass=" + targetClass.getName() +
                ", outputDir=" + outputDir +
                ", projectConfig=" + projectConfig +
                ']';
    }
}