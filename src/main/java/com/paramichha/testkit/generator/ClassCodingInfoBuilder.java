package com.paramichha.testkit.generator;

/**
 * Builds a {@link ClassCodingInfo} from a {@link TestSourceContext}.
 *
 * <p>This is the only interface that knows about both the test domain
 * ({@link TestSourceContext}) and the coding model ({@link ClassCodingInfo}).
 * Implementations contain all test-specific logic — what sections to generate,
 * what DataFactory values to embed, what assertions to write.
 *
 * <p>Coders know nothing about tests. Builders know nothing about Java syntax.
 *
 * @see TestClassBuilder
 */
public interface ClassCodingInfoBuilder {
    ClassCodingInfo build(TestSourceContext ctx);
}
