package com.paramichha.testkit;

import java.util.List;
import com.paramichha.testkit.model.ClassMeta;
import com.paramichha.testkit.model.TestData;

/**
 * Generates a {@link GeneratedTestClass} from class metadata and test data.
 * Implement to produce a different test style (e.g. Spock, Kotest).
 */
public interface TestGenerator {

    GeneratedTestClass generate(ClassMeta meta, TestData data, String targetPackage);

    /** Generate with custom template. Default implementation delegates to the 3-arg version. */
    default GeneratedTestClass generate(ClassMeta meta, TestData data,
                                        String targetPackage, TestKitTemplate template) {
        return generate(meta, data, targetPackage);
    }

    /** Generate with custom template and project type. */
    default GeneratedTestClass generate(ClassMeta meta, TestData data,
                                        String targetPackage, TestKitTemplate template,
                                        ProjectType projectType) {
        return generate(meta, data, targetPackage, template);
    }

    /** Generates multiple files — base + one per concern. */
    default List<GeneratedTestClass> generateAll(ClassMeta meta, TestData data,
                                                 String targetPackage, TestKitTemplate template) {
        return List.of(generate(meta, data, targetPackage, template));
    }

    /** Generates multiple files with project type awareness. */
    default List<GeneratedTestClass> generateAll(ClassMeta meta, TestData data,
                                                 String targetPackage, TestKitTemplate template,
                                                 ProjectType projectType) {
        return List.of(generate(meta, data, targetPackage, template, projectType));
    }
}