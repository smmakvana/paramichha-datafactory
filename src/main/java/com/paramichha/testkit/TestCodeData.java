package com.paramichha.testkit;

import com.paramichha.datafactory.GenerationMode;
import com.paramichha.testkit.model.ClassInfo;
import com.paramichha.testkit.model.TestFixture;

/**
 * Pipeline context — assembled by TestKit and passed through the generation chain.
 * <p>
 * Flow:
 * TestKit.of(Class)
 * → ClassInfo        (reader)
 * → TestFixture      (DataFactory values)
 * → TestCodeData     (this — everything bundled)
 * → GenerationResult (CodeGenerator)
 */
public final class TestCodeData {

    private final ClassInfo classInfo;
    private final TestFixture fixture;
    private final String targetPackage;
    private final TestKitTemplate template;
    private final ProjectType projectType;
    private final GenerationMode mode;
    private final Long seed;

    TestCodeData(ClassInfo classInfo, TestFixture fixture,
                 String targetPackage, TestKitTemplate template,
                 ProjectType projectType, GenerationMode mode, Long seed) {
        this.classInfo = classInfo;
        this.fixture = fixture;
        this.targetPackage = targetPackage;
        this.template = template;
        this.projectType = projectType;
        this.mode = mode;
        this.seed = seed;
    }

    public ClassInfo classInfo() {
        return classInfo;
    }

    public TestFixture fixture() {
        return fixture;
    }

    public String targetPackage() {
        return targetPackage;
    }

    public TestKitTemplate template() {
        return template;
    }

    public ProjectType projectType() {
        return projectType;
    }

    public GenerationMode mode() {
        return mode;
    }

    public Long seed() {
        return seed;
    }
}