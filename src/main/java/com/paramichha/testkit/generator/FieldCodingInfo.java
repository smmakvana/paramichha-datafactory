package com.paramichha.testkit.generator;

import java.util.List;

/**
 * Info needed to produce source code for a single field declaration.
 *
 * <pre>
 * // Test:
 * new FieldCoder().code(new FieldCodingInfo() {
 *     public String name()              { return "validator"; }
 *     public String typeName()          { return "Validator"; }
 *     public List<String> modifiers()   { return List.of("private", "static"); }
 *     public List<String> annotations() { return List.of(); }
 *     public String initializer()       { return null; }
 * });
 * // → "    private static Validator validator;\n"
 * </pre>
 */
public interface FieldCodingInfo {
    String        name();
    String        typeName();
    List<String>  modifiers();
    List<String>  annotations();
    String        initializer();   // null → no initializer; "= new Foo()" → with initializer
}
