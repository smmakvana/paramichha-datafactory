package com.paramichha.testkit;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.generator.CleanCodeGenerator;
import com.paramichha.testkit.model.ClassInfo;
import com.paramichha.testkit.model.TestFixture;

import java.util.List;

/**
 * Entry point for test generation.
 *
 * <pre>
 * TestKit.of(UserRequest.class)
 *        .withPackage("com.example.test")
 *        .generateTest()
 *        .writeTo(Path.of("src/test/java"));
 *
 * TestKit.of(classInfo)   // PSI / YAML path
 *        .generateTest();
 * </pre>
 */
public final class TestKit {

    private final Class<?> type;
    private final ClassInfo classInfo;
    private String targetPackage;
    private TestKitTemplate template = TestKitTemplate.DEFAULT;
    private ProjectType projectType = ProjectType.detect();
    private GenerationMode mode = GenerationMode.PRODUCTION;
    private Long seed = null;
    private final CodeGenerator generator = CleanCodeGenerator.INSTANCE;
    private final ClassReader reader = ReflectionClassReader.INSTANCE;
    private final TestDataProvider dataProvider = DefaultTestDataProvider.INSTANCE;

    // ── Constructors ──────────────────────────────────────────────────────

    private TestKit(Class<?> type) {
        this.type = type;
        this.classInfo = null;
        this.targetPackage = type.getPackageName();
    }

    private TestKit(ClassInfo classInfo) {
        this.type = null;
        this.classInfo = classInfo;
        this.targetPackage = classInfo.packageName();
    }

    // ── Entry points ──────────────────────────────────────────────────────

    public static TestKit of(Class<?> type) {
        if (type == null) throw new TestKitException("type must not be null");
        return new TestKit(type);
    }

    public static TestKit of(ClassInfo classInfo) {
        if (classInfo == null) throw new TestKitException("classInfo must not be null");
        return new TestKit(classInfo);
    }

    // ── Fluent config ─────────────────────────────────────────────────────

    public TestKit withPackage(String targetPackage) {
        if (targetPackage == null || targetPackage.isBlank())
            throw new TestKitException("targetPackage must not be blank");
        this.targetPackage = targetPackage;
        return this;
    }

    public TestKit withTemplate(TestKitTemplate template) {
        if (template == null) throw new TestKitException("template must not be null");
        this.template = template;
        return this;
    }

    public TestKit withProjectType(ProjectType projectType) {
        if (projectType == null) throw new TestKitException("projectType must not be null");
        this.projectType = projectType;
        return this;
    }

    public TestKit withMode(GenerationMode mode) {
        if (mode == null) throw new TestKitException("mode must not be null");
        this.mode = mode;
        return this;
    }

    public TestKit withSeed(long seed) {
        this.seed = seed;
        return this;
    }

    // ── Terminal — pipeline executes here ─────────────────────────────────

    public GenerationResult generateTest() {
        return generator.generate(buildPayload());
    }

    public List<GenerationResult> generateTests() {
        return generator.generateAll(buildPayload());
    }

    // ── Pipeline ──────────────────────────────────────────────────────────

    private TestCodeData buildPayload() {
        // Step 1 — resolve ClassInfo
        ClassInfo resolved = classInfo != null ? classInfo : reader.read(type);

        // Step 2 — load values via DataFactory
        TestFixture fixture = dataProvider.provide(resolved.type(), mode, seed);

        // Step 3 — assemble pipeline context
        return new TestCodeData(resolved, fixture, targetPackage, template, projectType, mode, seed);
    }
}