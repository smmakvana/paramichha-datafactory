package com.paramichha.testkit.api;

/**
 * Builds a TestClassPayload from ClassMeta by calling DataFactory.
 * The only place in the pipeline where DataFactory is called.
 *
 * Single responsibility:
 *   - For each member   → DataFactory → MemberData
 *   - For each method   → DataFactory → FunctionData per param
 *   - Collect validConstructorArgs for the creation smoke test
 *   - Never produce Java source
 */
public interface TestDataBuilder {

    TestClassPayload build(ClassMeta classMeta);
}
