package com.paramichha.testkit;

import java.util.List;

public interface TestKitX {
    /**
     * Generates multiple files — base class plus one per concern.
     *
     * <pre>
     * TestKit.of(UserRegistration.class)
     *        .split()
     *        .generateAll()
     *        .forEach(f -> f.writeTo(outputPath));
     * </pre>
     */
    List<GenerationResult> generateTests();

    GenerationResult generateTest();
}
