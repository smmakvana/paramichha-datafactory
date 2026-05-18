package com.paramichha.testkit;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.reader.DefaultClassReader;

/**
 * Request object for TestKit — carries the subject and all optional config.
 *
 * <pre>
 * // Minimal
 * TestKitRequest.of(UserService.class)
 *
 * // With overrides
 * TestKitRequest.of(UserService.class)
 *               .withProjectType(ProjectType.PLAIN_JAVA)
 *               .withSeed(42L)
 * </pre>
 */
public final class TestKitRequest {

    private final ClassMeta classMeta;
    private String targetPackage;
    private ProjectType projectType = ProjectType.detect();
    private TestKitTemplate template = TestKitTemplate.DEFAULT;
    private GenerationMode mode = GenerationMode.PRODUCTION;
    private Long seed = null;

    // ── Constructors ──────────────────────────────────────────────────────

    private TestKitRequest(ClassMeta classMeta) {
        this.classMeta = classMeta;
        this.targetPackage = classMeta.packageName();
    }

    // ── Entry points ──────────────────────────────────────────────────────

    /**
     * Reads ClassMeta from reflection.
     */
    public static TestKitRequest of(Class<?> type) {
        if (type == null) throw new TestKitException("type must not be null");
        return new TestKitRequest(DefaultClassReader.INSTANCE.read(type));
    }

    /**
     * Accepts pre-built ClassMeta — PSI, YAML, or any other source.
     */
    public static TestKitRequest of(ClassMeta classMeta) {
        if (classMeta == null) throw new TestKitException("classMeta must not be null");
        return new TestKitRequest(classMeta);
    }

    // ── Optional config ───────────────────────────────────────────────────

    public TestKitRequest withPackage(String targetPackage) {
        if (targetPackage == null || targetPackage.isBlank())
            throw new TestKitException("targetPackage must not be blank");
        this.targetPackage = targetPackage;
        return this;
    }

    public TestKitRequest withProjectType(ProjectType projectType) {
        if (projectType == null) throw new TestKitException("projectType must not be null");
        this.projectType = projectType;
        return this;
    }

    public TestKitRequest withTemplate(TestKitTemplate template) {
        if (template == null) throw new TestKitException("template must not be null");
        this.template = template;
        return this;
    }

    public TestKitRequest withMode(GenerationMode mode) {
        if (mode == null) throw new TestKitException("mode must not be null");
        this.mode = mode;
        return this;
    }

    public TestKitRequest withSeed(long seed) {
        this.seed = seed;
        return this;
    }


    // ── Accessors (package-private — TestKit reads these) ─────────────────

    ClassMeta classMeta() {
        return classMeta;
    }

    String targetPackage() {
        return targetPackage;
    }

    ProjectType projectType() {
        return projectType;
    }

    TestKitTemplate template() {
        return template;
    }

    GenerationMode mode() {
        return mode;
    }

    Long seed() {
        return seed;
    }
}