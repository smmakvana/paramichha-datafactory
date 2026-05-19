package com.paramichha.testkit.generator;

import com.paramichha.testkit.CodeGenerator;
import com.paramichha.testkit.GenerationResult;
import com.paramichha.testkit.TestCodeData;
import com.paramichha.testkit.TestKitTemplate;
import com.paramichha.testkit.model.*;

import com.paramichha.testkit.utils.TestNames;
import com.paramichha.testkit.utils.JavaNames;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates generation — unpacks {@link TestCodeData} and delegates
 * each section to a focused writer.
 * Never throws — always produces output even on DataFactory failure.
 */
public final class JavaTestCodeGenerator implements CodeGenerator {

    public static final JavaTestCodeGenerator INSTANCE =
            new JavaTestCodeGenerator(DefaultValueExtractor.INSTANCE);

    private final ValueExtractor valueExtractor;

    JavaTestCodeGenerator(ValueExtractor valueExtractor) {
        this.valueExtractor = valueExtractor;
    }

    // ── CodeGenerator ─────────────────────────────────────────────────────

    @Override
    public GenerationResult generate(TestCodeData payload) {
        ClassInfo    meta          = payload.classInfo();
        TestFixture  data          = payload.fixture();
        String       targetPackage = payload.targetPackage();
        boolean      isDev         = data.mode() == com.paramichha.datafactory.GenerationMode.DEV;
        String       className     = isDev
                ? meta.simpleName() + "DevTest"
                : meta.simpleName() + "Test";

        if (data.hasError()) {
            return new GenerationResult(className, targetPackage,
                    buildPlaceholder(meta, targetPackage, data.error(), className));
        }
        try {
            return buildFull(payload, className, isDev);
        } catch (Exception e) {
            return new GenerationResult(className, targetPackage,
                    buildPlaceholder(meta, targetPackage, e.getMessage(), className));
        }
    }

    @Override
    public List<GenerationResult> generateAll(TestCodeData payload) {
        TestFixture data = payload.fixture();
        if (data.hasError()) {
            String name = payload.classInfo().simpleName() + "Test";
            return List.of(new GenerationResult(name, payload.targetPackage(),
                    "// Generation failed: " + data.error()));
        }
        try {
            return buildSplit(payload);
        } catch (Exception e) {
            String name = payload.classInfo().simpleName() + "Test";
            return List.of(new GenerationResult(name, payload.targetPackage(),
                    "// Generation failed: " + e.getMessage()));
        }
    }

    // ── Single file ───────────────────────────────────────────────────────

    private GenerationResult buildFull(TestCodeData payload, String className, boolean isDev) {
        ClassInfo       meta          = payload.classInfo();
        TestFixture     data          = payload.fixture();
        String          targetPackage = payload.targetPackage();
        TestKitTemplate template      = payload.template();
        boolean         hasJakarta    = payload.projectType().hasValidator();

        Class<?>        type               = data.type();
        List<Object>    validInstanceParts = valueExtractor.extract(data.base(), type, meta.fields());
        InstantiationWriter instantiation  = new InstantiationWriter(meta);
        ImportCollector     imports        = buildImports(meta, type, data, validInstanceParts);

        StringBuilder sb = new StringBuilder();
        writeHeader(sb, meta, isDev);
        writePackage(sb, targetPackage);
        writeImports(sb, imports);
        writeClassOpen(sb, meta, className);
        if (hasJakarta) writeValidatorSetup(sb, template);
        writeValidInstance(sb, meta, validInstanceParts, instantiation);
        if (meta.constructorStyle() == ConstructorStyle.SETTER) writeSetterHelper(sb, meta);
        if (!meta.utilityClass()) {
            new CreationTestWriter(meta, instantiation, template)
                    .write(sb, data, validInstanceParts, valueExtractor);
            new MemberTestWriter(meta, instantiation, valueExtractor, template)
                    .write(sb, type, validInstanceParts, data.validAllPerField());
        }
        new MethodTestWriter(meta, instantiation, template)
                .write(sb, validInstanceParts, hasJakarta);
        sb.append("}\n");

        return new GenerationResult(className, targetPackage, sb.toString());
    }

    // ── Split files ───────────────────────────────────────────────────────

    private List<GenerationResult> buildSplit(TestCodeData payload) {
        ClassInfo       meta          = payload.classInfo();
        TestFixture     data          = payload.fixture();
        String          targetPackage = payload.targetPackage();
        TestKitTemplate template      = payload.template();
        boolean         hasJakarta    = payload.projectType().hasValidator();

        Class<?>     type      = data.type();
        String       className = meta.simpleName();
        List<Object> parts     = valueExtractor.extract(data.base(), type, meta.fields());
        InstantiationWriter inst = new InstantiationWriter(meta);
        String baseName = className + "Test";
        List<GenerationResult> files = new ArrayList<>();

        // 1. Base class
        StringBuilder base = new StringBuilder();
        writeHeader(base, meta, false);
        writePackage(base, targetPackage);
        addImports(base, meta, type, parts);
        base.append("\n@DisplayName(\"").append(className).append("\")\n");
        base.append("class ").append(baseName).append(" {\n\n");
        if (hasJakarta) writeValidatorSetup(base, template);
        base.append("    ").append(className).append(" ").append(TestNames.toVariable(className))
                .append(" =\n            ").append(inst.constructorCall(parts)).append(";\n\n");
        base.append("}\n");
        files.add(new GenerationResult(baseName, targetPackage, base.toString()));

        // 2. Creation file
        String creationName = template.resolve(template.creationClassName, className, "");
        StringBuilder cr = new StringBuilder();
        writeHeader(cr, meta, false);
        writePackage(cr, targetPackage);
        addImports(cr, meta, type, parts);
        cr.append("\n@DisplayName(\"")
                .append(template.resolve(template.creationDisplay, className, "")).append("\")\n");
        cr.append("class ").append(creationName).append(" extends ").append(baseName).append(" {\n\n");
        new CreationTestWriter(meta, inst, template).writeFlat(cr, data, parts, valueExtractor);
        cr.append("}\n");
        files.add(new GenerationResult(creationName, targetPackage, cr.toString()));

        // 3. One file per field
        MemberTestWriter mw = new MemberTestWriter(meta, inst, valueExtractor, template);
        List<FieldInfo> fields = meta.fields();
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo field      = fields.get(i);
            String    memberName = template.resolve(template.memberClassName, className, field.name());
            StringBuilder mb = new StringBuilder();
            writeHeader(mb, meta, false);
            writePackage(mb, targetPackage);
            addImports(mb, meta, type, parts);
            mb.append("\n@DisplayName(\"")
                    .append(template.resolve(template.memberDisplay, className, field.name()))
                    .append("\")\n");
            mb.append("class ").append(memberName).append(" extends ").append(baseName).append(" {\n\n");
            mw.writeFlat(mb, type, parts, field, i);
            mb.append("}\n");
            files.add(new GenerationResult(memberName, targetPackage, mb.toString()));
        }

        // 4. One file per method
        MethodTestWriter methodWriter = new MethodTestWriter(meta, inst, template);
        for (MethodInfo method : meta.methods()) {
            String methodClassName = template.resolveMethod(template.methodClassName, className, method.name());
            StringBuilder mm = new StringBuilder();
            writeHeader(mm, meta, false);
            writePackage(mm, targetPackage);
            addImports(mm, meta, type, parts);
            mm.append("\n@DisplayName(\"")
                    .append(template.resolveMethod(template.methodDisplay, className, method.name()))
                    .append("\"\n");
            mm.append("class ").append(methodClassName).append(" extends ").append(baseName).append(" {\n\n");
            methodWriter.writeFlat(mm, method, parts, hasJakarta);
            mm.append("}\n");
            files.add(new GenerationResult(methodClassName, targetPackage, mm.toString()));
        }

        return List.copyOf(files);
    }

    // ── Import helpers ────────────────────────────────────────────────────

    private ImportCollector buildImports(ClassInfo meta, Class<?> type,
                                         TestFixture data, List<Object> validInstanceParts) {
        ImportCollector ic = new ImportCollector();
        ic.addTypeImport(type);
        ic.addFieldTypeImports(meta.fields());
        ic.addValueImports(validInstanceParts);
        data.violations().forEach(v ->
                ic.addValueImports(valueExtractor.extract(v, type, meta.fields())));
        meta.methods().forEach(m -> ic.addAnnotationImports(m.returnType().annotations()));
        boolean needsDataValidation = meta.methods().stream().anyMatch(m ->
                m.returnType().isConstrained() ||
                        m.parameters().stream().anyMatch(p -> !p.validations().isEmpty()));
        if (needsDataValidation) ic.addRawImport("com.paramichha.utils.DataValidation");
        return ic;
    }

    private void addImports(StringBuilder sb, ClassInfo meta, Class<?> type, List<Object> parts) {
        ImportCollector ic = new ImportCollector();
        ic.addTypeImport(type);
        ic.addFieldTypeImports(meta.fields());
        ic.addValueImports(parts);
        meta.methods().forEach(m -> ic.addAnnotationImports(m.returnType().annotations()));
        boolean needsDataValidation = meta.methods().stream().anyMatch(m ->
                m.returnType().isConstrained() ||
                        m.parameters().stream().anyMatch(p -> !p.validations().isEmpty()));
        if (needsDataValidation) ic.addRawImport("com.paramichha.utils.DataValidation");
        for (String imp : ic.collect()) sb.append("import ").append(imp).append(";\n");
    }

    // ── Section writers ───────────────────────────────────────────────────

    private void writeHeader(StringBuilder sb, ClassInfo meta, boolean isDev) {
        sb.append("// Generated by TestKit — do not edit by hand.\n");
        if (isDev) {
            sb.append("// Mode: DEV — full type range, nullable fields.\n");
            sb.append("// Regenerate: TestKit.of(").append(meta.simpleName())
                    .append(".class).withMode(GenerationMode.DEV).generateTest().writeTo(...)\n");
        } else {
            sb.append("// Regenerate: TestKit.of(").append(meta.simpleName())
                    .append(".class).generateTest().writeTo(...)\n");
        }
    }

    private void writePackage(StringBuilder sb, String pkg) {
        sb.append("package ").append(pkg).append(";\n\n");
    }

    private void writeImports(StringBuilder sb, ImportCollector imports) {
        imports.collect().forEach(i -> sb.append("import ").append(i).append(";\n"));
        sb.append("\n");
    }

    private void writeClassOpen(StringBuilder sb, ClassInfo meta, String className) {
        sb.append("@DisplayName(\"").append(meta.simpleName()).append("\")\n");
        sb.append("class ").append(className).append(" {\n\n");
    }

    private void writeValidatorSetup(StringBuilder sb, TestKitTemplate template) {
        sb.append("    private static Validator ").append(template.validatorField).append(";\n\n");
        sb.append("    @BeforeAll\n");
        sb.append("    static void ").append(template.validatorSetup).append("() {\n");
        sb.append("        ").append(template.validatorField)
                .append(" = Validation.buildDefaultValidatorFactory().getValidator();\n");
        sb.append("    }\n\n");
    }

    private void writeValidInstance(StringBuilder sb, ClassInfo meta,
                                    List<Object> validInstanceParts,
                                    InstantiationWriter instantiation) {
        if (meta.fields().isEmpty()) return;
        String fieldName = TestNames.toVariable(meta.simpleName());
        sb.append("    ").append(meta.simpleName()).append(" ").append(fieldName).append(" =\n");
        sb.append("            ").append(instantiation.constructorCall(validInstanceParts))
                .append(";\n\n");
    }

    private void writeSetterHelper(StringBuilder sb, ClassInfo meta) {
        String name = meta.simpleName();
        sb.append("    private static ").append(name).append(" build").append(name).append("(");
        List<FieldInfo> fields = meta.fields();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(FieldTestWriter.sourceTypeName(fields.get(i).rawType()))
                    .append(" ").append(fields.get(i).name());
        }
        sb.append(") {\n");
        sb.append("        ").append(name).append(" obj = new ").append(name).append("();\n");
        for (FieldInfo f : fields) {
            sb.append("        obj.").append(JavaNames.setterName(f.name()))
                    .append("(").append(f.name()).append(");\n");
        }
        sb.append("        return obj;\n");
        sb.append("    }\n\n");
    }

    private String buildPlaceholder(ClassInfo meta, String targetPackage,
                                    String error, String className) {
        return "package " + targetPackage + ";\n\n"
                + "// TODO: " + className + "\n"
                + "// Could not generate — DataFactory failed for " + meta.simpleName() + "\n"
                + "// Reason: " + error + "\n"
                + "// Fix the fixture or DataFactory support and regenerate.\n"
                + "//\n"
                + "// class " + className + " {\n"
                + "//     // generation failed — no test content\n"
                + "// }\n";
    }
}