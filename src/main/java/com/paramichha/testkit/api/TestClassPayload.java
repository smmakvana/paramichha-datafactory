package com.paramichha.testkit.api;

import java.util.List;

/**
 * The complete input to the CodeWriter for generating a test class.
 * Contains ClassMeta (structure) + all DataFactory-generated values.
 *
 * Three-stage pipeline:
 *   ClassReader      → ClassMeta          (structure, no values)
 *   TestDataBuilder  → TestClassPayload   (structure + values, no source)
 *   CodeWriter       → GeneratedFile      (source, no logic)
 *
 * The CodeWriter only reads this — it never calls DataFactory or reflection.
 */
public interface TestClassPayload {

    ClassMeta classMeta();

    /**
     * Valid constructor args for the smoke test instance.
     * One value per primary constructor parameter.
     */
    List<Object> validConstructorArgs();

    /**
     * One MemberData per field — valid and invalid values.
     * Ordered to match classMeta().members().
     */
    List<MemberData> members();

    /**
     * One FunctionData per method — valid and invalid args.
     * Ordered to match classMeta().methods().
     */
    List<FunctionData> methods();

    /**
     * Whether payload was built successfully.
     * False means DataFactory could not produce even one valid instance.
     * CodeWriter produces a commented placeholder file.
     */
    boolean isValid();

    /** Null if isValid(). Reason DataFactory failed otherwise. */
    String errorMessage();
}
