package com.paramichha.testkit.api;

import java.util.List;

/**
 * Describes a class, record, interface, or enum — structure only, no values.
 * The root of the meta model.
 *
 * Source-agnostic — populated by any ClassReader implementation:
 *   ReflectionClassReader  → reads Class<?> via reflection
 *   PsiClassReader         → reads PsiClass via IntelliJ PSI
 *   YamlClassReader        → reads YAML for DevBoost
 *
 * The entire generation pipeline depends only on this interface.
 */
public interface ClassMeta {

    // ── Identity ──────────────────────────────────────────────────────────

    String simpleName();

    String packageName();

    String qualifiedName();

    ClassKind classType();           // CLASS, RECORD, INTERFACE, ENUM

    // ── Annotations ───────────────────────────────────────────────────────

    List<AnnotationInfo> annotations();   // @Service, @Entity, @Component etc

    // ── Hierarchy ─────────────────────────────────────────────────────────

    TypeInfo superType();                 // null for Object

    List<TypeInfo> interfaces();

    // ── Construction ──────────────────────────────────────────────────────

    ConstructionStyle constructionStyle();

    /** All constructors — primary constructor is first. */
    List<CallableMeta> constructors();

    /** The primary constructor TestKit uses to create instances. */
    CallableMeta primaryConstructor();

    // ── Members ───────────────────────────────────────────────────────────

    /** Non-static fields in declaration order. */
    List<MemberMeta> members();

    // ── Callables ─────────────────────────────────────────────────────────

    /** Public non-static non-Object methods. */
    List<CallableMeta> methods();

    /**
     * All callables — constructors + methods.
     * Useful when treating them uniformly.
     */
    List<CallableMeta> callables();

    // ── Flags ─────────────────────────────────────────────────────────────

    boolean isUtilityClass();    // all-static, no instances

    boolean isAbstract();

    boolean isFinal();
}
