package com.paramichha.testkit.generator;

import com.paramichha.testkit.CodeGenerator;
import com.paramichha.testkit.GenerationResult;
import com.paramichha.testkit.TestCodeData;
import com.paramichha.testkit.model.ConstructorStyle;
import com.paramichha.testkit.model.FieldInfo;
import com.paramichha.testkit.model.MethodInfo;
import com.paramichha.testkit.utils.JavaNames;
import com.paramichha.testkit.utils.TestNames;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin orchestrator — builds a {@link TestSourceContext} from the payload,
 * then delegates each section to its writer.
 *
 * Responsibility: sequencing only. No string building, no value extraction,
 * no import logic — all of that lives in {@link TestSourceContext} and the writers.
 */
public final class JavaTestCodeGenerator implements CodeGenerator {

    public static final JavaTestCodeGenerator INSTANCE = new JavaTestCodeGenerator();

    private JavaTestCodeGenerator() {}

    // ── CodeGenerator ─────────────────────────────────────────────────────

    @Override
    public GenerationResult generate(TestCodeData payload) {
        if (payload.fixture().hasError()) return placeholder(payload);
        try {
            return buildFull(TestSourceContext.from(payload));
        } catch (Exception e) {
            return placeholder(payload, e.getMessage());
        }
    }

    @Override
    public List<GenerationResult> generateAll(TestCodeData payload) {
        if (payload.fixture().hasError()) return List.of(placeholder(payload));
        try {
            return buildSplit(TestSourceContext.from(payload));
        } catch (Exception e) {
            return List.of(placeholder(payload, e.getMessage()));
        }
    }

    // ── Single file ───────────────────────────────────────────────────────

    private GenerationResult buildFull(TestSourceContext ctx) {
        StringBuilder sb = new StringBuilder();

        SourceWriter.writeHeader(sb, ctx);
        SourceWriter.writePackage(sb, ctx);
        SourceWriter.writeImports(sb, ctx);
        SourceWriter.writeClassOpen(sb, ctx);
        if (ctx.hasJakarta)           SourceWriter.writeValidatorSetup(sb, ctx);
        SourceWriter.writeValidInstance(sb, ctx);
        if (isSetter(ctx))            SourceWriter.writeSetterHelper(sb, ctx);

        if (!ctx.classInfo.utilityClass()) {
            new CreationTestWriter(ctx.classInfo, ctx.instantiation, ctx.template)
                    .write(sb, ctx.fixture, ctx.validParts, DefaultValueExtractor.INSTANCE);
            new MemberTestWriter(ctx.classInfo, ctx.instantiation, DefaultValueExtractor.INSTANCE, ctx.template)
                    .write(sb, ctx.fixture.type(), ctx.validParts, ctx.validAllPerField());
        }
        new MethodTestWriter(ctx.classInfo, ctx.instantiation, ctx.template, ctx.seed)
                .write(sb, ctx.validParts, ctx.hasJakarta);

        sb.append("}\n");
        return new GenerationResult(ctx.className, ctx.targetPackage, sb.toString());
    }

    // ── Split files ───────────────────────────────────────────────────────

    private List<GenerationResult> buildSplit(TestSourceContext ctx) {
        List<GenerationResult> files   = new ArrayList<>();
        String                 base    = ctx.simpleName() + "Test";

        // 1. Base class
        files.add(buildBase(ctx, base));

        // 2. Creation
        files.add(buildCreation(ctx, base));

        // 3. One per field
        List<FieldInfo> fields = ctx.fields();
        for (int i = 0; i < fields.size(); i++)
            files.add(buildMember(ctx, base, fields.get(i), i));

        // 4. One per method
        for (MethodInfo method : ctx.classInfo.methods())
            files.add(buildMethod(ctx, base, method));

        return List.copyOf(files);
    }

    private GenerationResult buildBase(TestSourceContext ctx, String baseName) {
        StringBuilder sb = new StringBuilder();
        SourceWriter.writeHeader(sb, ctx);
        SourceWriter.writePackage(sb, ctx);
        SourceWriter.writeImports(sb, ctx);
        sb.append("\n@DisplayName(\"").append(ctx.simpleName()).append("\")\n");
        sb.append("class ").append(baseName).append(" {\n\n");
        if (ctx.hasJakarta) SourceWriter.writeValidatorSetup(sb, ctx);
        SourceWriter.writeValidInstance(sb, ctx);
        sb.append("}\n");
        return new GenerationResult(baseName, ctx.targetPackage, sb.toString());
    }

    private GenerationResult buildCreation(TestSourceContext ctx, String baseName) {
        String creationName = ctx.template.resolve(ctx.template.creationClassName, ctx.simpleName(), "");
        StringBuilder sb = new StringBuilder();
        SourceWriter.writeHeader(sb, ctx);
        SourceWriter.writePackage(sb, ctx);
        SourceWriter.writeImports(sb, ctx);
        sb.append("\n@DisplayName(\"")
                .append(ctx.template.resolve(ctx.template.creationDisplay, ctx.simpleName(), ""))
                .append("\")\n");
        sb.append("class ").append(creationName).append(" extends ").append(baseName).append(" {\n\n");
        new CreationTestWriter(ctx.classInfo, ctx.instantiation, ctx.template)
                .writeFlat(sb, ctx.fixture, ctx.validParts, DefaultValueExtractor.INSTANCE);
        sb.append("}\n");
        return new GenerationResult(creationName, ctx.targetPackage, sb.toString());
    }

    private GenerationResult buildMember(TestSourceContext ctx, String baseName,
                                         FieldInfo field, int index) {
        String memberName = ctx.template.resolve(ctx.template.memberClassName, ctx.simpleName(), field.name());
        StringBuilder sb = new StringBuilder();
        SourceWriter.writeHeader(sb, ctx);
        SourceWriter.writePackage(sb, ctx);
        SourceWriter.writeImports(sb, ctx);
        sb.append("\n@DisplayName(\"")
                .append(ctx.template.resolve(ctx.template.memberDisplay, ctx.simpleName(), field.name()))
                .append("\")\n");
        sb.append("class ").append(memberName).append(" extends ").append(baseName).append(" {\n\n");
        new MemberTestWriter(ctx.classInfo, ctx.instantiation, DefaultValueExtractor.INSTANCE, ctx.template)
                .writeFlat(sb, ctx.fixture.type(), ctx.validParts, field, index);
        sb.append("}\n");
        return new GenerationResult(memberName, ctx.targetPackage, sb.toString());
    }

    private GenerationResult buildMethod(TestSourceContext ctx, String baseName, MethodInfo method) {
        String methodClassName = ctx.template.resolveMethod(ctx.template.methodClassName, ctx.simpleName(), method.name());
        StringBuilder sb = new StringBuilder();
        SourceWriter.writeHeader(sb, ctx);
        SourceWriter.writePackage(sb, ctx);
        SourceWriter.writeImports(sb, ctx);
        sb.append("\n@DisplayName(\"")
                .append(ctx.template.resolveMethod(ctx.template.methodDisplay, ctx.simpleName(), method.name()))
                .append("\")\n");
        sb.append("class ").append(methodClassName).append(" extends ").append(baseName).append(" {\n\n");
        new MethodTestWriter(ctx.classInfo, ctx.instantiation, ctx.template, ctx.seed)
                .writeFlat(sb, method, ctx.validParts, ctx.hasJakarta);
        sb.append("}\n");
        return new GenerationResult(methodClassName, ctx.targetPackage, sb.toString());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private boolean isSetter(TestSourceContext ctx) {
        return ctx.classInfo.constructorStyle() == ConstructorStyle.SETTER;
    }

    private GenerationResult placeholder(TestCodeData payload) {
        return placeholder(payload, payload.fixture().error());
    }

    private GenerationResult placeholder(TestCodeData payload, String error) {
        String name = payload.classInfo().simpleName() + "Test";
        String pkg  = payload.targetPackage();
        String src  = "package " + pkg + ";\n\n"
                + "// TODO: " + name + "\n"
                + "// Could not generate — DataFactory failed for " + payload.classInfo().simpleName() + "\n"
                + "// Reason: " + error + "\n"
                + "// Fix the fixture or DataFactory support and regenerate.\n"
                + "// class " + name + " {\n"
                + "//     // generation failed — no test content\n"
                + "// }\n";
        return new GenerationResult(name, pkg, src);
    }
}