package com.paramichha.testkit;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.data.DataFactoryTestDataProvider;
import com.paramichha.testkit.generator.DefaultTestGenerator;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.TestData;
import com.paramichha.testkit.reader.DefaultClassReader;

import java.util.List;

/**
 * Entry point for single-class test generation.
 *
 * <pre>
 * TestKit.of(UserRequest.class)
 *        .withPackage("com.example.test")
 *        .generate()
 *        .writeTo(Path.of("src/test/java"));
 *
 * TestKit.of(classMeta)           // PSI / YAML path
 *        .generate();
 * </pre>
 */
public final class TestKits implements TestKitGenerator {

    private final ClassMeta classMeta;
    private final TestDataProvider dataProvider = DataFactoryTestDataProvider.INSTANCE;
    private final TestGenerator generator = DefaultTestGenerator.INSTANCE;
    private String targetPackage;
    private Long seed = null;
    private GenerationMode mode = GenerationMode.PRODUCTION;
    private TestKitTemplate template = TestKitTemplate.DEFAULT;
    private boolean split = false;
    private ProjectType projectType = ProjectType.detect();

    // ── Constructors ──────────────────────────────────────────────────────

    private TestKits(ClassMeta classMeta) {
        this.classMeta     = classMeta;
        this.targetPackage = classMeta.packageName();
    }

    // ── Entry points ──────────────────────────────────────────────────────

    /** Reads ClassMeta from reflection then delegates to of(ClassMeta). */
    public static TestKits of(Class<?> type) {
        if (type == null) throw new TestKitException("type must not be null");
        return new TestKits(DefaultClassReader.INSTANCE.read(type));
    }

    /** Accepts pre-built ClassMeta — PSI, YAML, or manual path. */
    public static TestKits of(ClassMeta classMeta) {
        if (classMeta == null) throw new TestKitException("classMeta must not be null");
        return new TestKits(classMeta);
    }

    // ── Config ────────────────────────────────────────────────────────────

    public TestKits withPackage(String packageName) {
        if (packageName == null || packageName.isBlank())
            throw new TestKitException("packageName must not be blank");
        this.targetPackage = packageName;
        return this;
    }

    public TestKits withSeed(long seed) {
        this.seed = seed;
        return this;
    }

    public TestKits withMode(GenerationMode mode) {
        if (mode == null) throw new TestKitException("mode must not be null");
        this.mode = mode;
        return this;
    }

    public TestKits withTemplate(TestKitTemplate template) {
        if (template == null) throw new TestKitException("template must not be null");
        this.template = template;
        return this;
    }

    public TestKits withProjectType(ProjectType projectType) {
        if (projectType == null) throw new TestKitException("projectType must not be null");
        this.projectType = projectType;
        return this;
    }

    public TestKits split() {
        this.split = true;
        return this;
    }

    // ── Generate ──────────────────────────────────────────────────────────

    @Override
    public List<GeneratedTestClass> generateAll() {
        try {
            TestData data = buildTestData();
            return generator.generateAll(classMeta, data, targetPackage, template, projectType);
        } catch (Exception e) {
            String className = classMeta.simpleName() + "Test";
            String src = "package " + targetPackage + ";\n\n// Generation failed: " + e.getMessage() + "\n";
            return List.of(new GeneratedTestClass(className, targetPackage, src));
        }
    }

    @Override
    public GeneratedTestClass generate() {
        try {
            TestData data = buildTestData();
            return generator.generate(classMeta, data, targetPackage, template, projectType);
        } catch (Exception e) {
            String className = classMeta.simpleName() + "Test";
            String source = "package " + targetPackage + ";\n\n"
                    + "// TODO: " + className + "\n"
                    + "// Generation failed for " + classMeta.simpleName() + "\n"
                    + "// Reason: " + e.getMessage() + "\n"
                    + "// Fix the issue and regenerate.\n";
            return new GeneratedTestClass(className, targetPackage, source);
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private TestData buildTestData() {
        return (dataProvider instanceof DataFactoryTestDataProvider dfp)
                ? dfp.provide(classMeta.type(), mode, seed)
                : dataProvider.provide(classMeta.type());
    }
}