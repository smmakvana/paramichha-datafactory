package com.paramichha.testkit.api;

/**
 * Reads a source and produces ClassMeta.
 *
 * Implementations:
 *   ReflectionClassReader  → reads Class<?> via Java reflection
 *   PsiClassReader         → reads PsiClass via IntelliJ PSI
 *   YamlClassReader        → reads YAML for DevBoost
 */
public interface ClassReader<S> {

    ClassMeta read(S source);
}
