package com.paramichha.testkit.api;

/** How instances of this type are constructed. */
public enum ConstructionStyle {
    RECORD,          // canonical record constructor
    ALL_ARGS,        // all-args constructor
    LOMBOK_BUILDER,  // MyClass.builder().field(x).build()
    SETTER,          // no-args constructor + setters
    FACTORY,         // static factory method
    UNKNOWN
}
