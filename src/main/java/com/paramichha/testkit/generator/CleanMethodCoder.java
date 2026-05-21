package com.paramichha.testkit.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles test methods for one method's nested class from a {@link MethodPayload}.
 *
 * Pure string assembly — no DataFactory, no TestSourceContext, no ClassInfo.
 * All data arrives pre-resolved via {@link MethodPayload}.
 *
 * Switch on kind → delegates to private method per kind.
 * Each returns a {@code List<MethodCodingInfo>} — the test methods inside the nested class.
 */
final class CleanMethodCoder {

    List<MethodCodingInfo> code(MethodPayload p) {
        List<MethodCodingInfo> methods = new ArrayList<>();

        if (p.wrapperRecord != null)
            methods.add(raw(p.wrapperRecord));

        List<MethodCodingInfo> kindMethods = switch (p.kind) {
            case COMMAND              -> command(p);
            case QUERY                -> query(p);
            case MUTATOR, TRANSFORMER -> mutatorOrTransformer(p);
            case OPERATION            -> mutatorOrTransformer(p);
        };

        methods.addAll(kindMethods);
        return List.copyOf(methods);
    }

    // ── COMMAND — void, no params ─────────────────────────────────────────

    private List<MethodCodingInfo> command(MethodPayload p) {
        List<MethodCodingInfo> methods = new ArrayList<>();
        String call = p.fieldName + "." + p.methodName + "()";

        methods.add(method(
                "testExecutesWithoutException", "void", List.of(),
                List.of("@Test", "@DisplayName(\"executes without exception\")"),
                List.of(),
                "assertThatCode(() -> " + call + ").doesNotThrowAnyException();"
        ));

        if (p.hasJakarta) {
            methods.add(method(
                    "testObjectRemainsValidAfterExecution", "void", List.of(),
                    List.of("@Test", "@DisplayName(\"object remains valid after execution\")"),
                    List.of(),
                    p.fieldName + "." + p.methodName + "();\n"
                            + "assertThat(" + p.validatorField + ".validate(" + p.fieldName + ")).isEmpty();"
            ));
        }
        return methods;
    }

    // ── QUERY — has return, no params ─────────────────────────────────────

    private List<MethodCodingInfo> query(MethodPayload p) {
        List<MethodCodingInfo> methods = new ArrayList<>();
        String call = p.fieldName + "." + p.methodName + "()";

        StringBuilder body = new StringBuilder();
        if (p.isVoid) {
            body.append("assertThatCode(() -> ").append(call).append(").doesNotThrowAnyException();");
        } else {
            body.append(p.returnTypeName).append(" result = ").append(call).append(";\n");
            if (!p.returnAssertion.isBlank()) body.append(p.returnAssertion);
        }

        methods.add(method(
                "testReturnsResult", "void", List.of(),
                List.of("@Test", "@DisplayName(\"" + p.methodName + " returns a result\")"),
                List.of(), body.toString().stripTrailing()
        ));

        if (!p.isVoid) {
            methods.add(method(
                    "testReturnsConsistentResult", "void", List.of(),
                    List.of("@Test", "@DisplayName(\"" + p.methodName + " returns consistent result\")"),
                    List.of(),
                    "assertThat(" + call + ").isEqualTo(" + call + ");"
            ));
        }
        return methods;
    }

    // ── MUTATOR / TRANSFORMER / OPERATION — has params ────────────────────

    private List<MethodCodingInfo> mutatorOrTransformer(MethodPayload p) {
        List<MethodCodingInfo> methods = new ArrayList<>();
        boolean anyConstrained = p.params.stream().anyMatch(pp -> pp.isConstrained);
        String  call           = p.fieldName + "." + p.methodName + "(" + p.paramCall + ")";

        // testAcceptsValidInput
        StringBuilder acceptsBody = new StringBuilder();
        if (p.isVoid) {
            acceptsBody.append("assertThatCode(() -> ").append(call)
                    .append(").doesNotThrowAnyException();");
        } else {
            acceptsBody.append(p.returnTypeName).append(" result = ").append(call).append(";\n");
            if (!p.returnAssertion.isBlank()) acceptsBody.append(p.returnAssertion);
        }

        methods.add(method(
                "testAcceptsValidInput", "void", List.of(),
                List.of("@ParameterizedTest(name = \"{0}\")",
                        "@MethodSource(\"" + p.validSourceName + "\")",
                        "@DisplayName(\"" + p.methodName + " accepts valid input\")"),
                buildParamCodingInfos(p), acceptsBody.toString().stripTrailing()
        ));

        // testRejectsInvalidInput — only if any param is constrained
        if (anyConstrained) {
            StringBuilder rejectsBody = new StringBuilder();
            p.enforcementHints.forEach(h -> rejectsBody.append(h).append("\n"));
            rejectsBody.append("assertThatThrownBy(() -> ").append(call).append(")\n")
                    .append("        .isInstanceOf(Exception.class);");

            methods.add(method(
                    "testRejectsInvalidInput", "void", List.of(),
                    List.of("@ParameterizedTest(name = \"{0}\")",
                            "@MethodSource(\"" + p.invalidSourceName + "\")",
                            "@DisplayName(\"" + p.methodName + " rejects invalid input\")"),
                    buildParamCodingInfos(p), rejectsBody.toString().stripTrailing()
            ));
        }

        // valid stream
        methods.add(stream(p.validSourceName, p.validRows));

        // invalid stream
        if (anyConstrained)
            methods.add(stream(p.invalidSourceName, p.invalidRows));

        return methods;
    }

    // ── Stream method helper ──────────────────────────────────────────────

    private static MethodCodingInfo stream(String name, List<String> rows) {
        StringBuilder body = new StringBuilder("return Stream.of(\n");
        for (int i = 0; i < rows.size(); i++) {
            body.append("    ").append(rows.get(i));
            body.append(i < rows.size() - 1 ? ",\n" : "\n");
        }
        body.append(");");
        return method(name, "Stream<Arguments>", List.of("static"), List.of(), List.of(),
                body.toString());
    }

    // ── MethodCodingInfo factories ────────────────────────────────────────

    private static List<ParameterCodingInfo> buildParamCodingInfos(MethodPayload p) {
        List<ParameterCodingInfo> result = new ArrayList<>();
        result.add(param("description", "String"));
        for (MethodPayload.ParamPayload pp : p.params)
            result.add(param(pp.name, pp.typeName));
        return List.copyOf(result);
    }

    private static MethodCodingInfo method(String name, String returnType,
                                            List<String> modifiers, List<String> annotations,
                                            List<ParameterCodingInfo> params, String body) {
        return new MethodCodingInfo() {
            public String name()                          { return name; }
            public String returnType()                    { return returnType; }
            public List<String> modifiers()               { return modifiers; }
            public List<String> annotations()             { return annotations; }
            public List<ParameterCodingInfo> parameters() { return params; }
            public String body()                          { return body; }
            public boolean isEmpty()                      { return false; }
        };
    }

    private static MethodCodingInfo raw(String line) {
        return new MethodCodingInfo() {
            public String name()                          { return ""; }
            public String returnType()                    { return ""; }
            public List<String> modifiers()               { return List.of(); }
            public List<String> annotations()             { return List.of(); }
            public List<ParameterCodingInfo> parameters() { return List.of(); }
            public String body()                          { return line; }
            public boolean isEmpty()                      { return true; }
        };
    }

    private static ParameterCodingInfo param(String name, String typeName) {
        return new ParameterCodingInfo() {
            public String name()              { return name; }
            public String typeName()          { return typeName; }
            public List<String> annotations() { return List.of(); }
        };
    }
}
