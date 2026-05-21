package com.paramichha.testkit.generator;

import com.paramichha.testkit.model.MethodInfo;
import com.paramichha.testkit.model.MethodKind;
import com.paramichha.testkit.model.ParameterInfo;
import com.paramichha.testkit.model.ReturnTypeInfo;

import java.util.List;

/**
 * Pre-resolved payload for one method's nested test class.
 *
 * All DataFactory calls happen in {@link MethodPayloadFactory} before this is created.
 * Everything here is a plain string or primitive — no DataFactory, no TestSourceContext.
 *
 * Flow:
 *   MethodInfo + TestSourceContext
 *       → MethodPayloadFactory.build()   ← DataFactory calls happen HERE
 *       → MethodPayload                  ← pure data from here down
 *       → CleanMethodCoder.code()        ← pure string assembly
 *       → List<MethodCodingInfo>
 */
final class MethodPayload {

    // ── Identity ──────────────────────────────────────────────────────────

    final MethodKind kind;
    final String     methodName;
    final String     nestedClassName;
    final String     displayName;
    final String     fieldName;          // instance field or class name if utility

    // ── Template-resolved names ───────────────────────────────────────────

    final String     validSourceName;    // e.g. "validMergeInputs"
    final String     invalidSourceName;  // e.g. "invalidMergeInputs"

    // ── Return type — fully resolved ──────────────────────────────────────

    final String     returnTypeName;     // e.g. "ObjectAnnotatedService", "void", "double"
    final boolean    isVoid;
    final boolean    returnIsPrimitive;
    final String     returnAssertion;    // pre-built assertion line(s) or ""
    final String     wrapperRecord;      // "private record XxxResult(...) {}" or null

    // ── Parameters ────────────────────────────────────────────────────────

    final List<ParamPayload> params;
    final String             paramSignature;  // "ObjectAnnotatedService other, String newName"
    final String             paramCall;       // "other, newName"

    // ── Pre-built data rows ───────────────────────────────────────────────

    final List<String> validRows;         // each: "Arguments.of(\"valid merge\", ...)"
    final List<String> invalidRows;       // each: "Arguments.of(\"invalid other\", ...)"
    final List<String> enforcementHints;  // each: "// Enforce @NotNull: DataValidation.notNull(other);"

    // ── Context ───────────────────────────────────────────────────────────

    final boolean    hasJakarta;
    final String     validatorField;

    MethodPayload(MethodKind kind, String methodName, String nestedClassName,
                  String displayName, String fieldName,
                  String validSourceName, String invalidSourceName,
                  String returnTypeName, boolean isVoid, boolean returnIsPrimitive,
                  String returnAssertion, String wrapperRecord,
                  List<ParamPayload> params, String paramSignature, String paramCall,
                  List<String> validRows, List<String> invalidRows,
                  List<String> enforcementHints,
                  boolean hasJakarta, String validatorField) {
        this.kind              = kind;
        this.methodName        = methodName;
        this.nestedClassName   = nestedClassName;
        this.displayName       = displayName;
        this.fieldName         = fieldName;
        this.validSourceName   = validSourceName;
        this.invalidSourceName = invalidSourceName;
        this.returnTypeName    = returnTypeName;
        this.isVoid            = isVoid;
        this.returnIsPrimitive = returnIsPrimitive;
        this.returnAssertion   = returnAssertion;
        this.wrapperRecord     = wrapperRecord;
        this.params            = params;
        this.paramSignature    = paramSignature;
        this.paramCall         = paramCall;
        this.validRows         = validRows;
        this.invalidRows       = invalidRows;
        this.enforcementHints  = enforcementHints;
        this.hasJakarta        = hasJakarta;
        this.validatorField    = validatorField;
    }

    // ── Nested: per-parameter data ────────────────────────────────────────

    static final class ParamPayload {
        final String  name;
        final String  typeName;
        final boolean isConstrained;

        ParamPayload(String name, String typeName, boolean isConstrained) {
            this.name          = name;
            this.typeName      = typeName;
            this.isConstrained = isConstrained;
        }
    }
}
