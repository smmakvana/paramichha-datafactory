package com.paramichha.testkit.api;

/**
 * Converts a TestClassPayload into a GeneratedFile.
 * Pure source building — no DataFactory calls, no reflection.
 *
 * Generic enough to generate anything:
 *   TestCodeWriter    → JUnit 5 test class
 *   DtoCodeWriter     → DTO from entity
 *   DocCodeWriter     → Javadoc / markdown
 *   ScaffoldWriter    → DevBoost service/controller/repository
 *   OpenApiWriter     → OpenAPI spec
 */
public interface CodeWriter {

    GeneratedFile write(TestClassPayload payload);
}
