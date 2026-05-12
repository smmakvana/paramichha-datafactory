package com.paramichha.testkit;

/**
 * Controls all generated names and display strings.
 * All strings support tokens replaced at generation time.
 */
public final class TestKitTemplate {

    // ── Validator ─────────────────────────────────────────────────────────
    public String validatorField   = "validator";
    public String validatorSetup   = "setUpValidator";

    // ── Level 2 — Creation ────────────────────────────────────────────────
    public String creationClassName      = "{ClassName}CreationTest";
    public String creationDisplay        = "{ClassName} object creation";
    public String canBeCreated           = "testCanBeCreated";
    public String canBeCreatedDisplay    = "{className} can be created";
    public String holdsExpectedValues    = "testHoldsExpectedValues";
    public String holdsExpectedDisplay   = "{className} holds expected values";
    public String isValid                = "testIsValid";
    public String isValidDisplay         = "{className} is valid";

    // ── Level 3 — Member ──────────────────────────────────────────────────
    public String memberClassName        = "{ClassName}{MemberName}Test";
    public String memberDisplay          = "{memberName} in {ClassName}";
    public String acceptsMethod          = "testAccepts{MemberName}";
    public String acceptsDisplay         = "{memberName} accepts valid value";
    public String rejectsMethod          = "testRejects{MemberName}";
    public String rejectsDisplay         = "{memberName} rejects invalid value";
    public String validDataSource        = "valid{MemberName}s";
    public String invalidDataSource      = "invalid{MemberName}s";

    // ── Level 4 — Method ──────────────────────────────────────────────────
    public String methodClassName              = "{ClassName}Method{MethodName}Test";
    public String methodDisplay                = "{methodName} in {ClassName}";
    public String testExecutesWithoutException = "testExecutesWithoutException";
    public String testObjectRemainsValid       = "testObjectRemainsValidAfterExecution";
    public String testReturnsResult            = "testReturnsResult";
    public String testReturnsConsistentResult  = "testReturnsConsistentResult";
    public String testAcceptsValidInput        = "testAcceptsValidInput";
    public String testRejectsInvalidInput      = "testRejectsInvalidInput";
    public String validInputsSource            = "valid{MethodName}Inputs";
    public String invalidInputsSource          = "invalid{MethodName}Inputs";

    // ── Level 5 — Scenario ────────────────────────────────────────────────
    public String scenarioClassName      = "{ClassName}ScenarioTest";
    public String scenarioDisplay        = "{ClassName} scenarios";

    // ── Default singleton ─────────────────────────────────────────────────
    public static final TestKitTemplate DEFAULT = new TestKitTemplate();

    // ── Token resolution ──────────────────────────────────────────────────

    public String resolve(String template, String className, String memberName) {
        return template
                .replace("{ClassName}",  className)
                .replace("{className}",  lower(className))
                .replace("{MemberName}", upper(memberName))
                .replace("{memberName}", memberName);
    }

    public String resolveMethod(String template, String className, String methodName) {
        return template
                .replace("{ClassName}",  className)
                .replace("{className}",  lower(className))
                .replace("{MethodName}", upper(methodName))
                .replace("{methodName}", methodName);
    }

    private static String upper(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String lower(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
