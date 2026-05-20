package com.paramichha.testkit.generator;

import java.util.List;

/**
 * Info needed to produce source code for a complete Java class.
 *
 * <p>Handles both top-level and nested classes via {@link #isNested()}:
 * <ul>
 *   <li>Top-level ({@code isNested() == false}): {@link #header()},
 *       {@link #packageName()}, and {@link #imports()} are written first.
 *   <li>Nested ({@code isNested() == true}): those three are skipped;
 *       the class is written directly inside its parent.
 * </ul>
 *
 * <p>{@link #nestedClasses()} is recursive — nested classes may themselves
 * contain nested classes (e.g. a private record inside a @Nested test class).
 *
 * <p>{@link #extendsClass()} is null for standard generation; set for split mode.
 */
public interface ClassCodingInfo {

    // ── Top-level only ────────────────────────────────────────────────────

    boolean       isNested();       // false → write header + package + imports first
    String        header();         // null for nested — comment block at top of file
    String        packageName();    // null for nested
    List<String>  imports();        // empty for nested

    // ── Class declaration ─────────────────────────────────────────────────

    String        className();
    String        displayName();    // used for @DisplayName annotation value
    List<String>  modifiers();      // e.g. ["class"], ["final", "class"]
    List<String>  annotations();    // e.g. ["@Nested", "@DisplayName(\"Creation\")"]
    String        extendsClass();   // null → no extends; "BaseTest" → extends BaseTest

    // ── Class body ────────────────────────────────────────────────────────

    List<FieldCodingInfo>   fields();
    List<MethodCodingInfo>  methods();
    List<ClassCodingInfo>   nestedClasses();  // recursive — same type
}
