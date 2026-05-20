package com.paramichha.testkit.generator;

import java.util.List;

/**
 * Info needed to produce source code for a single method.
 *
 * <p>{@link #isEmpty()} signals this method should be emitted as a
 * commented-out TODO block rather than a live method — used when a field
 * has no constraints yet and the reject test is a placeholder.
 *
 * <pre>
 * // Test:
 * new MethodCoder().code(new MethodCodingInfo() {
 *     public String name()                      { return "testCanBeCreated"; }
 *     public String returnType()                { return "void"; }
 *     public List<String> modifiers()           { return List.of(); }
 *     public List<String> annotations()         { return List.of("@Test", "@DisplayName(\"can be created\")"); }
 *     public List<ParameterCodingInfo> params() { return List.of(); }
 *     public String body()                      { return "assertThat(obj).isNotNull();"; }
 *     public boolean isEmpty()                  { return false; }
 * });
 * </pre>
 */
public interface MethodCodingInfo {
    String                      name();
    String                      returnType();
    List<String>                modifiers();
    List<String>                annotations();
    List<ParameterCodingInfo>   parameters();
    String                      body();
    boolean                     isEmpty();    // true → emit as // TODO comment block
}
