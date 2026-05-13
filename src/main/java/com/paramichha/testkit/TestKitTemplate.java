package com.paramichha.testkit;

import com.paramichha.utils.Texts;
import com.paramichha.utils.Tokens;

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
        return Tokens.resolve(template,
                "ClassName",  className,
                "className",  Texts.decapitalize(className),
                "MemberName", Texts.capitalize(memberName),
                "memberName", memberName);
    }

    public String resolveMethod(String template, String className, String methodName) {
        return Tokens.resolve(template,
                "ClassName",  className,
                "className",  Texts.decapitalize(className),
                "MethodName", Texts.capitalize(methodName),
                "methodName", methodName);
    }

}
