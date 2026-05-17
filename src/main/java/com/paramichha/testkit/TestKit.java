package com.paramichha.testkit;

import java.util.List;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.data.DataFactoryTestDataProvider;
import com.paramichha.testkit.generator.DefaultTestGenerator;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.TestData;
import com.paramichha.testkit.reader.DefaultClassReader;

/**
 * Entry point for single-class test generation.
 *
 * <pre>
 * TestKit.of(UserRequest.class)
 *        .withPackage("com.example.test")
 *        .generate()
 *        .writeTo(Path.of("src/test/java"));
 * </pre>
 */
public final class TestKit implements TestKitGenerator {

    private final Class<?> type;
    private final ClassReader reader;
    private final TestDataProvider dataProvider;
    private final TestGenerator generator;
    private String targetPackage;
    private Long seed = null;
    private GenerationMode mode = GenerationMode.PRODUCTION;
    private TestKitTemplate template = TestKitTemplate.DEFAULT;
    private boolean split = false;
    private ProjectType projectType = ProjectType.detect();

    private TestKit(Class<?> type, ClassReader reader,
                    TestDataProvider dataProvider, TestGenerator generator) {
        this.type         = type;
        this.reader       = reader;
        this.dataProvider = dataProvider;
        this.generator    = generator;
        this.targetPackage = type.getPackageName();
    }

    public static TestKit of(Class<?> type) {
        if (type == null) throw new TestKitException("type must not be null");
        return new TestKit(type,
                DefaultClassReader.INSTANCE,
                DataFactoryTestDataProvider.INSTANCE,
                DefaultTestGenerator.INSTANCE);
    }

    /** Inject custom implementations — for testing or extension. */
    public static TestKit of(Class<?> type, ClassReader reader,
                             TestDataProvider dataProvider, TestGenerator generator) {
        if (type == null) throw new TestKitException("type must not be null");
        return new TestKit(type, reader, dataProvider, generator);
    }

    public TestKit withPackage(String packageName) {
        if (packageName == null || packageName.isBlank())
            throw new TestKitException("packageName must not be blank");
        this.targetPackage = packageName;
        return this;
    }

    /** Seeds the DataFactory random generator for deterministic test data. */
    public TestKit withSeed(long seed) {
        this.seed = seed;
        return this;
    }

    /** Sets the DataFactory generation mode. Defaults to PRODUCTION. */
    public TestKit withMode(GenerationMode mode) {
        if (mode == null) throw new TestKitException("mode must not be null");
        this.mode = mode;
        return this;
    }

    /** Sets a custom template for generated names and display strings. */
    public TestKit withTemplate(TestKitTemplate template) {
        if (template == null) throw new TestKitException("template must not be null");
        this.template = template;
        return this;
    }

    /** Sets the project type for test generation. Auto-detected by default. */
    public TestKit withProjectType(ProjectType projectType) {
        if (projectType == null) throw new TestKitException("projectType must not be null");
        this.projectType = projectType;
        return this;
    }

    /**
     * Opt-in to split mode — generates one file per concern.
     * Use {@link #generateAll()} as the terminal method.
     */
    public TestKit split() {
        this.split = true;
        return this;
    }

    /**
     * Generates multiple files — base class plus one per concern.
     * Call after {@link #split()}.
     *
     * <pre>
     * TestKit.of(UserRegistration.class)
     *        .split()
     *        .generateAll()
     *        .forEach(f -> f.writeTo(outputPath));
     * </pre>
     */
    @Override
    public List<GeneratedTestClass> generateAll() {
        try {
            ClassMeta meta = reader.read(type);
            TestData data = (dataProvider instanceof DataFactoryTestDataProvider dfp)
                    ? dfp.provide(type, mode, seed)
                    : dataProvider.provide(type);
            return generator.generateAll(meta, data, targetPackage, template, projectType);
        } catch (Exception e) {
            String className = type.getSimpleName() + "Test";
            String src = "package " + targetPackage + ";\n\n// Generation failed: " + e.getMessage() + "\n";
            return List.of(new GeneratedTestClass(className, targetPackage, src));
        }
    }

    @Override
    public GeneratedTestClass generate() {
        try {
            ClassMeta meta = reader.read(type);
            TestData data = (dataProvider instanceof DataFactoryTestDataProvider dfp)
                    ? dfp.provide(type, mode, seed)
                    : dataProvider.provide(type);
            return generator.generate(meta, data, targetPackage, template, projectType);
        } catch (Exception e) {
            String className = type.getSimpleName() + "Test";
            String source = "package " + targetPackage + ";\n\n"
                    + "// TODO: " + className + "\n"
                    + "// Generation failed for " + type.getSimpleName() + "\n"
                    + "// Reason: " + e.getMessage() + "\n"
                    + "// Fix the issue and regenerate.\n";
            return new GeneratedTestClass(className, targetPackage, source);
        }
    }
}