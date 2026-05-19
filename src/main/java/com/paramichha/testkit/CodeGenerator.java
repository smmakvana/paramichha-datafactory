package com.paramichha.testkit;

import java.util.List;

/**
 * Generates test source from a fully assembled {@link TestCodeData} pipeline context.
 * Implement to produce a different test style (e.g. Spock, Kotest).
 */
public interface CodeGenerator {
    GenerationResult          generate(TestCodeData payload);
    List<GenerationResult>    generateAll(TestCodeData payload);
}