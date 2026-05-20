package com.paramichha.testkit.generator;

import java.util.List;

/**
 * Info needed to produce source code for a single method parameter.
 *
 * <pre>
 * // Test:
 * new ParameterCoder().code(new ParameterCodingInfo() {
 *     public String name()              { return "email"; }
 *     public String typeName()          { return "String"; }
 *     public List<String> annotations() { return List.of(); }
 * });
 * </pre>
 */
public interface ParameterCodingInfo {
    String        name();
    String        typeName();
    List<String>  annotations();
}
