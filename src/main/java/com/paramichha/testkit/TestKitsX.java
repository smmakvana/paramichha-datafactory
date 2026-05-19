//package com.paramichha.testkit;
//
//import com.paramichha.datafactory.GenerationMode;
//import com.paramichha.testkit.generator.JavaTestCodeGenerator;
//import com.paramichha.testkit.model.ClassInfo;
//import com.paramichha.testkit.model.TestFixture;
//
//import java.util.List;
//
///**
// * Entry point for single-class test generation.
// *
// * <pre>
// * TestKit.of(UserRequest.class)
// *        .withPackage("com.example.test")
// *        .generate()
// *        .writeTo(Path.of("src/test/java"));
// *
// * TestKit.of(classMeta)           // PSI / YAML path
// *        .generate();
// * </pre>
// */
//public final class TestKitsX implements TestKitX {
//
//    private final TestDataProvider dataProvider = DefaultTestDataProvider.INSTANCE;
//    private final CodeGenerator generator = JavaTestCodeGenerator.INSTANCE;
//    private final ClassInfo classInfo;
//    private String targetPackage;
//    private Long seed;
//    private ProjectType projectType;
//    private GenerationMode mode;
//    private TestKitTemplate template = TestKitTemplate.DEFAULT;
//
//    // ── Constructors ──────────────────────────────────────────────────────
//
//    private TestKitsX(ClassInfo classInfo, Long seed, ProjectType projectType, GenerationMode mode) {
//        this.classInfo = classInfo;
//        this.targetPackage = classInfo.packageName();
//        this.seed = seed;
//        this.projectType = projectType;
//        this.mode = mode;
//    }
//
//    // ── Entry points ──────────────────────────────────────────────────────
//
//    /** Reads ClassMeta from reflection then delegates to of(ClassMeta). */
//    public static TestKitsX of(Class<?> type) {
//        if (type == null) throw new TestKitException("type must not be null");
//        return new TestKitsX(ReflectionClassReader.INSTANCE.read(type), 69L, ProjectType.detect(), GenerationMode.PRODUCTION);
//    }
//
//    /** Accepts pre-built ClassMeta — PSI, YAML, or manual path. */
//    public static TestKitsX of(ClassInfo classInfo) {
//        if (classInfo == null) throw new TestKitException("classMeta must not be null");
//        return new TestKitsX(classInfo, 69L, ProjectType.detect(), GenerationMode.PRODUCTION);
//    }
//
//    // ── Config ────────────────────────────────────────────────────────────
//
//    public TestKitsX withPackage(String packageName) {
//        if (packageName == null || packageName.isBlank())
//            throw new TestKitException("packageName must not be blank");
//        this.targetPackage = packageName;
//        return this;
//    }
//
//    public TestKitsX withSeed(long seed) {
//        this.seed = seed;
//        return this;
//    }
//
//    public TestKitsX withMode(GenerationMode mode) {
//        if (mode == null) throw new TestKitException("mode must not be null");
//        this.mode = mode;
//        return this;
//    }
//
//    public TestKitsX withTemplate(TestKitTemplate template) {
//        if (template == null) throw new TestKitException("template must not be null");
//        this.template = template;
//        return this;
//    }
//
//    public TestKitsX withProjectType(ProjectType projectType) {
//        if (projectType == null) throw new TestKitException("projectType must not be null");
//        this.projectType = projectType;
//        return this;
//    }
//
//    // ── Generate ──────────────────────────────────────────────────────────
//
//    @Override
//    public List<GenerationResult> generateTests() {
//        try {
//            TestFixture data = buildTestData();
//            return generator.generateAll(classInfo, data, targetPackage, template, projectType);
//        } catch (Exception e) {
//            String className = classInfo.simpleName() + "Test";
//            String src = "package " + targetPackage + ";\n\n// Generation failed: " + e.getMessage() + "\n";
//            return List.of(new GenerationResult(className, targetPackage, src));
//        }
//    }
//
//    @Override
//    public GenerationResult generateTest() {
//        try {
//            TestFixture data = buildTestData();
//            return generator.generate(classInfo, data, targetPackage, template, projectType);
//        } catch (Exception e) {
//            String className = classInfo.simpleName() + "Test";
//            String source = "package " + targetPackage + ";\n\n"
//                    + "// TODO: " + className + "\n"
//                    + "// Generation failed for " + classInfo.simpleName() + "\n"
//                    + "// Reason: " + e.getMessage() + "\n"
//                    + "// Fix the issue and regenerate.\n";
//            return new GenerationResult(className, targetPackage, source);
//        }
//    }
//
//    // ── Internal ──────────────────────────────────────────────────────────
//
//    private TestFixture buildTestData() {
//        return (dataProvider instanceof DefaultTestDataProvider dfp)
//                ? dfp.provide(classInfo.type(), mode, seed)
//                : dataProvider.provide(classInfo.type());
//    }
//}