package com.paramichha.testkit.api;

import java.util.List;

/**
 * Information about a variable's type — field, parameter, or return type.
 * Replaces raw Class<?> everywhere in the model.
 *
 * Source-agnostic:
 *   reflection → rawType() is Class<?>
 *   PSI        → rawType() is PsiType
 *   YAML/text  → rawType() is String (fully qualified name)
 */
public interface TypeInfo {

    /** The raw underlying type. Cast based on your source. */
    Object rawType();

    String simpleName();

    String qualifiedName();

    /** Semantic category — what drives assertion and generation decisions. */
    VariableKind variableType();

    /** Generic type arguments — List<String> gives [TypeInfo for String]. */
    List<TypeInfo> typeArguments();

    /** For collections — the element type. List<String> → TypeInfo for String. */
    TypeInfo elementType();

    boolean isVoid();

    boolean isPrimitive();

    boolean isNullable();

    boolean isCollection();

    boolean isMap();

    boolean isDomain();

    /** Annotations on type-use — e.g. the @NotNull in List<@NotNull String>. */
    List<AnnotationInfo> typeUseAnnotations();
}
