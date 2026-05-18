package com.paramichha.testkit.api;

/**
 * What kind of callable this is.
 * CONSTRUCTOR is a special callable — same model as methods, no return type.
 */
public enum CallableKind {
    CONSTRUCTOR,  // same name as class, no return type, params may be empty
    FUNCTION      // everything else — has name, return type (void or not), params may be empty
}
