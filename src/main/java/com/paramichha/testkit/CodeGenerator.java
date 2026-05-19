package com.paramichha.testkit;

import java.util.List;

import com.paramichha.testkit.model.ReflectionClassInfo;
import com.paramichha.testkit.model.TestData;

/**
 * Generates a {@link GenerationResult} from class metadata and test data.
 * Implement to produce a different test style (e.g. Spock, Kotest).
 */
public interface CodeGenerator {

    GenerationResult generate(ReflectionClassInfo meta, TestData data, String targetPackage);

    /** Generate with custom template. Default implementation delegates to the 3-arg version. */
    default GenerationResult generate(ReflectionClassInfo meta, TestData data,
                                      String targetPackage, TestKitTemplate template) {
        return generate(meta, data, targetPackage);
    }

    /** Generate with custom template and project type. */
    default GenerationResult generate(ReflectionClassInfo meta, TestData data,
                                      String targetPackage, TestKitTemplate template,
                                      ProjectType projectType) {
        return generate(meta, data, targetPackage, template);
    }

    /** Generates multiple files — base + one per concern. */
    default List<GenerationResult> generateAll(ReflectionClassInfo meta, TestData data,
                                               String targetPackage, TestKitTemplate template) {
        return List.of(generate(meta, data, targetPackage, template));
    }

    /** Generates multiple files with project type awareness. */
    default List<GenerationResult> generateAll(ReflectionClassInfo meta, TestData data,
                                               String targetPackage, TestKitTemplate template,
                                               ProjectType projectType) {
        return List.of(generate(meta, data, targetPackage, template, projectType));
    }
}