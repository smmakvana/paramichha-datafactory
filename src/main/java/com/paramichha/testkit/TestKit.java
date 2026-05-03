package com.paramichha.testkit;

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
public final class TestKit {

    private final Class<?> type;
    private final ClassReader reader;
    private final TestDataProvider dataProvider;
    private final TestGenerator generator;
    private String targetPackage;
    private Long seed = null;
    private GenerationMode mode = GenerationMode.PRODUCTION;

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

    public GeneratedTestClass generate() {
        try {
            ClassMeta meta = reader.read(type);
            TestData data = (dataProvider instanceof DataFactoryTestDataProvider dfp)
                    ? dfp.provide(type, mode, seed)
                    : dataProvider.provide(type);
            return generator.generate(meta, data, targetPackage);
        } catch (Exception e) {
            // Never fail — return a placeholder so the file is always written
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
