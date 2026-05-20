package com.paramichha.testkit.generator;

/**
 * Produces Java source code from a coding spec.
 *
 * <p>Each implementation is responsible for exactly one Java construct —
 * a class, a method, a field, or a parameter.
 * Coders are stateless, composable, and independently testable.
 *
 * <pre>
 * new MethodCoder().code(methodCodingInfo)     → String
 * new ClassCoder().code(classCodingInfo)       → String
 * new FieldCoder().code(fieldCodingInfo)       → String
 * </pre>
 *
 * @param <T> the coding info type this coder handles
 */
public interface Coder<T> {
    String code(T spec);
}
