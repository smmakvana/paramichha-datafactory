package com.paramichha.testkit;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.data.DefaultTestDataProvider;
import com.paramichha.testkit.generator.JavaCodeGenerator;
import com.paramichha.testkit.model.ReflectionClassInfo;
import com.paramichha.testkit.model.TestData;

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
public final class TestKits implements TestKit {

    private final TestDataProvider dataProvider = DefaultTestDataProvider.INSTANCE;
    private final CodeGenerator generator = JavaCodeGenerator.INSTANCE;
    private final ReflectionClassInfo reflectionClassInfo;
    private String targetPackage;
    private Long seed;
    private ProjectType projectType;
    private GenerationMode mode;
    private TestKitTemplate template = TestKitTemplate.DEFAULT;

    // ── Constructors ──────────────────────────────────────────────────────

    private TestKits(ReflectionClassInfo reflectionClassInfo, Long seed, ProjectType projectType, GenerationMode mode) {
        this.reflectionClassInfo = reflectionClassInfo;
        this.targetPackage = reflectionClassInfo.packageName();
        this.seed = seed;
        this.projectType = projectType;
        this.mode = mode;
    }

    // ── Entry points ──────────────────────────────────────────────────────

    /** Reads ClassMeta from reflection then delegates to of(ClassMeta). */
    public static TestKits of(Class<?> type) {
        if (type == null) throw new TestKitException("type must not be null");
        return new TestKits(ReflactionClassReader.INSTANCE.read(type), 69L, ProjectType.detect(), GenerationMode.PRODUCTION);
    }

    /** Accepts pre-built ClassMeta — PSI, YAML, or manual path. */
    public static TestKits of(ReflectionClassInfo reflectionClassInfo) {
        if (reflectionClassInfo == null) throw new TestKitException("classMeta must not be null");
        return new TestKits(reflectionClassInfo, 69L, ProjectType.detect(), GenerationMode.PRODUCTION);
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

    // ── Generate ──────────────────────────────────────────────────────────

    @Override
    public List<GenerationResult> generateTests() {
        try {
            TestData data = buildTestData();
            return generator.generateAll(reflectionClassInfo, data, targetPackage, template, projectType);
        } catch (Exception e) {
            String className = reflectionClassInfo.simpleName() + "Test";
            String src = "package " + targetPackage + ";\n\n// Generation failed: " + e.getMessage() + "\n";
            return List.of(new GenerationResult(className, targetPackage, src));
        }
    }

    @Override
    public GenerationResult generateTest() {
        try {
            TestData data = buildTestData();
            return generator.generate(reflectionClassInfo, data, targetPackage, template, projectType);
        } catch (Exception e) {
            String className = reflectionClassInfo.simpleName() + "Test";
            String source = "package " + targetPackage + ";\n\n"
                    + "// TODO: " + className + "\n"
                    + "// Generation failed for " + reflectionClassInfo.simpleName() + "\n"
                    + "// Reason: " + e.getMessage() + "\n"
                    + "// Fix the issue and regenerate.\n";
            return new GenerationResult(className, targetPackage, source);
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private TestData buildTestData() {
        return (dataProvider instanceof DefaultTestDataProvider dfp)
                ? dfp.provide(reflectionClassInfo.type(), mode, seed)
                : dataProvider.provide(reflectionClassInfo.type());
    }
}