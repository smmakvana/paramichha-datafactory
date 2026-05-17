package com.paramichha.testkit;

import java.util.List;

public interface TestKitGenerator {
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
    List<GeneratedTestClass> generateAll();

    GeneratedTestClass generate();
}
