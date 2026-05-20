package com.paramichha.testkit.generator;

import com.paramichha.testkit.ProjectType;
import com.paramichha.testkit.TestCodeData;
import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.ClassInfo;
import com.paramichha.testkit.model.FieldInfo;
import com.paramichha.testkit.model.TestFixture;

import java.util.List;
import java.util.Map;

/**
 * Pre-extracted generation context — assembled once from {@link TestCodeData},
 * then passed as read-only input to every writer.
 *
 * Writers receive this and write. No extraction, no decisions inside writers.
 *
 * Flow:
 *   TestCodeData → TestSourceContext → writers → GenerationResult
 */
final class TestSourceContext {

    final ClassInfo             classInfo;
    final TestFixture           fixture;
    final String                targetPackage;
    final TestKitTemplate       template;
    final ProjectType           projectType;
    final boolean               hasJakarta;
    final boolean               isDev;
    final String                className;      // e.g. "UserRequestTest" or "UserRequestDevTest"
    final String                instanceName;   // e.g. "userRequest"
    final List<Object>          validParts;     // per-field values from base valid instance
    final InstantiationWriter   instantiation;
    final ImportCollector       imports;
    final long                  seed;           // effective seed — flows into every DataFactory call

    private TestSourceContext(
            ClassInfo classInfo, TestFixture fixture,
            String targetPackage, TestKitTemplate template, ProjectType projectType,
            boolean isDev, String className, String instanceName,
            List<Object> validParts, InstantiationWriter instantiation,
            ImportCollector imports, long seed) {
        this.classInfo      = classInfo;
        this.fixture        = fixture;
        this.targetPackage  = targetPackage;
        this.template       = template;
        this.projectType    = projectType;
        this.hasJakarta     = projectType.hasValidator();
        this.isDev          = isDev;
        this.className      = className;
        this.instanceName   = instanceName;
        this.validParts     = validParts;
        this.instantiation  = instantiation;
        this.imports        = imports;
        this.seed           = seed;
    }

    // ── Factory ───────────────────────────────────────────────────────────

    static TestSourceContext from(TestCodeData payload) {
        ClassInfo       classInfo     = payload.classInfo();
        TestFixture     fixture       = payload.fixture();
        String          targetPackage = payload.targetPackage();
        TestKitTemplate template      = payload.template();
        ProjectType     projectType   = payload.projectType();

        // Resolve seed once — propagated into every DataFactory call downstream
        long effectiveSeed = payload.seed() != null
                ? payload.seed()
                : com.paramichha.utils.Seeds.stableHash(classInfo.type());

        boolean isDev        = fixture.mode() == com.paramichha.datafactory.GenerationMode.DEV;
        String  className    = classInfo.simpleName() + (isDev ? "DevTest" : "Test");
        String  instanceName = com.paramichha.testkit.utils.TestNames.toVariable(classInfo.simpleName());

        ValueExtractor      extractor     = DefaultValueExtractor.INSTANCE;
        List<Object>        validParts    = extractor.extract(fixture.base(), fixture.type(), classInfo.fields());
        InstantiationWriter instantiation = new InstantiationWriter(classInfo, effectiveSeed);
        ImportCollector     imports       = buildImports(classInfo, fixture, validParts, extractor);

        return new TestSourceContext(
                classInfo, fixture, targetPackage, template, projectType,
                isDev, className, instanceName, validParts, instantiation, imports, effectiveSeed);
    }

    // ── Convenience ───────────────────────────────────────────────────────

    Map<String, List<?>> validAllPerField() { return fixture.validAllPerField(); }
    List<FieldInfo>      fields()           { return classInfo.fields();         }
    String               simpleName()       { return classInfo.simpleName();     }

    // ── Imports ───────────────────────────────────────────────────────────

    private static ImportCollector buildImports(ClassInfo classInfo, TestFixture fixture,
                                                List<Object> validParts, ValueExtractor extractor) {
        ImportCollector ic = new ImportCollector();
        ic.addTypeImport(fixture.type());
        ic.addFieldTypeImports(classInfo.fields());
        ic.addValueImports(validParts);
        fixture.violations().forEach(v ->
                ic.addValueImports(extractor.extract(v, fixture.type(), classInfo.fields())));
        classInfo.methods().forEach(m -> ic.addAnnotationImports(m.returnType().annotations()));
        boolean needsDataValidation = classInfo.methods().stream().anyMatch(m ->
                m.returnType().isConstrained() ||
                        m.parameters().stream().anyMatch(p -> !p.validations().isEmpty()));
        if (needsDataValidation) ic.addRawImport("com.paramichha.utils.DataValidation");
        return ic;
    }
}