package com.paramichha.testkit.model;

/**
 * Classifies a method purely by its signature — no business knowledge required.
 */
public enum MethodKind {
    /** void method()          — no params, no return */
    COMMAND,
    /** void method(T)         — mutates via param, no return */
    MUTATOR,
    /** R method()             — query, no params */
    QUERY,
    /** R method(T)            — transforms one input to output */
    TRANSFORMER,
    /** R method(T1, T2...)    — multi-param operation */
    OPERATION
}
