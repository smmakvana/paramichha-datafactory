package com.paramichha.testkit;

/**
 * Controls all generated names and display strings.
 *
 * <p>Every string is a template supporting these tokens:
 * <pre>
 *   {ClassName}   — e.g. Circle
 *   {className}   — e.g. circle
 *   {MemberName}  — e.g. Radius
 *   {memberName}  — e.g. radius
 *   {MethodName}  — e.g. Scale
 *   {methodName}  — e.g. scale
 *   {MethodA}     — e.g. Scale
 *   {methodA}     — e.g. scale
 *   {MethodB}     — e.g. Translate
 *   {methodB}     — e.g. translate
 * </pre>
 *
 * <p>Usage:
 * <pre>
 *   TestKitTemplate t = new TestKitTemplate();
 *   t.isValid = "isCompletelyValid";
 *   TestKit.of(Circle.class).withTemplate(t).generate();
 * </pre>
 */
public final class TestKitTemplate {

    // ── Validator ─────────────────────────────────────────────────────────
    /** Field name for the Jakarta Validator instance. */
    public String validatorField   = "validator";
    /** Setup method name for @BeforeAll validator initialisation. */
    public String validatorSetup   = "setUpValidator";

    // ── Level 2 — Creation ────────────────────────────────────────────────
    /** Nested class name. Token: {ClassName} */
    public String creationClassName      = "{ClassName}CreationTest";
    /** @DisplayName for creation nested class. Token: {ClassName} */
    public String creationDisplay        = "{ClassName} object creation";

    /** Method name — proves object can be instantiated. */
    public String canBeCreated           = "canBeCreated";
    /** @DisplayName. Token: {className} */
    public String canBeCreatedDisplay    = "{className} can be created";

    /** Method name — proves field values are set correctly. */
    public String holdsExpectedValues    = "holdsExpectedValues";
    /** @DisplayName. Token: {className} */
    public String holdsExpectedDisplay   = "{className} holds expected values";

    /** Method name — proves Jakarta validation passes. Only emitted when class has annotations. */
    public String isValid                = "isValid";
    /** @DisplayName. Token: {className} */
    public String isValidDisplay         = "{className} is valid";

    // ── Level 3 — Member ──────────────────────────────────────────────────
    /** Nested class name. Token: {MemberName} */
    public String memberClassName        = "{ClassName}{MemberName}Test";
    /** @DisplayName. Tokens: {memberName}, {ClassName} */
    public String memberDisplay          = "{memberName} in {ClassName}";

    /** Method name for valid parameterized test. Token: {MemberName} */
    public String acceptsMethod          = "accepts{MemberName}";
    /** @DisplayName. Token: {memberName} */
    public String acceptsDisplay         = "{memberName} accepts valid value";

    /** Method name for invalid parameterized test. Token: {MemberName} */
    public String rejectsMethod          = "rejects{MemberName}";
    /** @DisplayName. Token: {memberName} */
    public String rejectsDisplay         = "{memberName} rejects invalid value";

    /** Static data source method for valid values. Token: {MemberName} */
    public String validDataSource        = "valid{MemberName}s";
    /** Static data source method for invalid values. Token: {MemberName} */
    public String invalidDataSource      = "invalid{MemberName}s";

    // ── Level 4 — Method (future) ─────────────────────────────────────────
    /** Nested class name. Token: {MethodName} */
    public String methodClassName        = "{ClassName}{MethodName}Test";
    /** @DisplayName. Tokens: {methodName}, {ClassName} */
    public String methodDisplay          = "{methodName} in {ClassName}";

    /** Method name for valid input test. */
    public String returnsResult          = "returnsResultForValidInput";
    /** @DisplayName. Token: {methodName} */
    public String returnsDisplay         = "{methodName} returns result for valid input";

    /** Method name for invalid input test. Only emitted when input has annotations. */
    public String handlesInvalid         = "handlesInvalidInputGracefully";
    /** @DisplayName. Token: {methodName} */
    public String handlesDisplay         = "{methodName} handles invalid input gracefully";

    // ── Level 5 — Chain (future) ──────────────────────────────────────────
    /** Nested class name. Tokens: {MethodA}, {MethodB} */
    public String chainClassName         = "{ClassName}ScenarioTest";
    /** @DisplayName. Tokens: {methodA}, {methodB}, {ClassName} */
    public String chainDisplay           = "{methodA} then {methodB} in {ClassName}";

    /** Method name inside chain test. */
    public String chainMethod            = "resultCanBeUsedAsNextInput";
    /** @DisplayName. Tokens: {methodA}, {methodB} */
    public String chainMethodDisplay     = "{methodA} result feeds {methodB} input";

    // ── Default singleton ─────────────────────────────────────────────────
    public static final TestKitTemplate DEFAULT = new TestKitTemplate();

    // ── Token resolution ──────────────────────────────────────────────────

    /**
     * Resolves {ClassName}, {className}, {MemberName}, {memberName} tokens.
     * Pass empty string for memberName when not applicable.
     */
    public String resolve(String template, String className, String memberName) {
        return template
                .replace("{ClassName}",  className)
                .replace("{className}",  lower(className))
                .replace("{MemberName}", upper(memberName))
                .replace("{memberName}", memberName);
    }

    /**
     * Resolves {ClassName}, {MethodA}, {methodA}, {MethodB}, {methodB} tokens.
     * Pass empty string for methodB when not a chain.
     */
    public String resolveMethod(String template, String className,
                                 String methodA, String methodB) {
        return template
                .replace("{ClassName}", className)
                .replace("{MethodA}",   upper(methodA))
                .replace("{methodA}",   methodA)
                .replace("{MethodB}",   upper(methodB))
                .replace("{methodB}",   methodB);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private static String upper(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String lower(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
