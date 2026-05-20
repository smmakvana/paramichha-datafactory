package com.paramichha.testkit.generator;

import com.paramichha.testkit.CodeGenerator;
import com.paramichha.testkit.GenerationResult;
import com.paramichha.testkit.TestCodeData;

import java.util.List;

/**
 * Drop-in replacement for {@link JavaTestCodeGenerator}.
 *
 * <p>Flow:
 * <pre>
 * TestCodeData
 *   → TestSourceContext   (pre-extracts all data: values, imports, instantiation)
 *   → TestClassBuilder    (builds ClassCodingInfo tree — test logic only)
 *   → ClassCoder          (produces Java source — syntax only, no test knowledge)
 *   → GenerationResult
 * </pre>
 *
 * <p>To swap in: change {@code JavaTestCodeGenerator.INSTANCE} to
 * {@code CleanCodeGenerator.INSTANCE} in {@code TestKit}.
 *
 * <p>To extend: provide a custom {@link ClassCodingInfoBuilder} to
 * {@link #CleanCodeGenerator(ClassCodingInfoBuilder)}.
 */
public final class CleanCodeGenerator implements CodeGenerator {

    public static final CleanCodeGenerator INSTANCE = new CleanCodeGenerator(new TestClassBuilder());

    private final ClassCodingInfoBuilder builder;
    private final ClassCoder             coder = new ClassCoder();

    public CleanCodeGenerator(ClassCodingInfoBuilder builder) {
        this.builder = builder;
    }

    @Override
    public GenerationResult generate(TestCodeData payload) {
        if (payload.fixture().hasError()) return placeholder(payload, payload.fixture().error());
        try {
            TestSourceContext ctx    = TestSourceContext.from(payload);
            ClassCodingInfo   info   = builder.build(ctx);
            String            source = coder.code(info);
            return new GenerationResult(info.className(), info.packageName(), source);
        } catch (Exception e) {
            return placeholder(payload, e.getMessage());
        }
    }

    @Override
    public List<GenerationResult> generateAll(TestCodeData payload) {
        // TODO not suppored yet!
        return List.of(generate(payload));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private GenerationResult placeholder(TestCodeData payload, String error) {
        String name = payload.classInfo().simpleName() + "Test";
        String pkg  = payload.targetPackage();
        return new GenerationResult(name, pkg,
                "package " + pkg + ";\n\n"
                + "// TODO: " + name + "\n"
                + "// Generation failed for " + payload.classInfo().simpleName() + "\n"
                + "// Reason: " + error + "\n"
                + "// Fix and regenerate.\n");
    }
}
