package com.paramichha.testkit.generator;

import java.util.List;

/**
 * Info needed to produce source code for a complete Java class — top-level or nested.
 *
 * <p>{@link #isNested()} controls whether header/package/imports are emitted.
 *
 * <p>{@link #rawBody()} is an escape hatch for sections not yet fully modelled
 * as {@link MethodCodingInfo} — when non-null, {@link ClassCoder} emits it
 * directly inside the class braces instead of iterating fields/methods/nestedClasses.
 */
public interface ClassCodingInfo {

    // ── Top-level only ────────────────────────────────────────────────────

    boolean       isNested();
    String        header();         // null for nested
    String        packageName();    // null for nested
    List<String>  imports();        // empty for nested

    // ── Class declaration ─────────────────────────────────────────────────

    String        className();
    String        displayName();
    List<String>  modifiers();
    List<String>  annotations();
    String        extendsClass();   // nullable

    // ── Class body ────────────────────────────────────────────────────────

    List<FieldCodingInfo>   fields();
    List<MethodCodingInfo>  methods();
    List<ClassCodingInfo>   nestedClasses();
}
