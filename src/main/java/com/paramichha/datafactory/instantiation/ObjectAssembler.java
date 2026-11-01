package com.paramichha.datafactory.instantiation;

import com.paramichha.datafactory.constraint.FieldDescriptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Assembles an instance of any class from a field-value map by delegating to the first
 * {@link ObjectCreation} that can handle it.
 *
 * <p>The winning strategy per class is cached so {@code canHandle()} reflection is only
 * paid once per class — not once per call.
 */
public final class ObjectAssembler {

    private static final List<ObjectCreation> STRATEGIES = List.of(
            LombokObjectCreation.INSTANCE,
            RecordObjectCreation.INSTANCE,
            ConstructorObjectCreation.INSTANCE,
            PojoObjectCreation.INSTANCE
    );

    private static final ConcurrentHashMap<Class<?>, ObjectCreation> STRATEGY_CACHE
            = new ConcurrentHashMap<>();

    private ObjectAssembler() {
    }

    public static <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                                    Map<String, Object> values) throws Exception {
        // Early guards — surface unsupported types with clear messages before any reflection
        if (type.isInterface()) {
            throw new com.paramichha.datafactory.DataFactoryException(
                    com.paramichha.datafactory.ErrorType.NOT_INSTANTIABLE,
                    "Cannot instantiate interface '" + type.getName()
                    + "' — provide a concrete implementation via .with()");
        }
        if (java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            throw new com.paramichha.datafactory.DataFactoryException(
                    com.paramichha.datafactory.ErrorType.NOT_INSTANTIABLE,
                    "Cannot instantiate abstract class '" + type.getName()
                    + "' — provide a concrete subclass via .with()");
        }
        if (type.isMemberClass() && !java.lang.reflect.Modifier.isStatic(type.getModifiers())) {
            throw new com.paramichha.datafactory.DataFactoryException(
                    com.paramichha.datafactory.ErrorType.NOT_INSTANTIABLE,
                    "Cannot instantiate non-static inner class '" + type.getName()
                    + "' — it requires an enclosing instance. Make it static or provide it via .with()");
        }

        // #3 — resolve outside computeIfAbsent to avoid caching a broken entry
        //       when orElseThrow fires inside the lambda on some JVM versions
        ObjectCreation strategy = STRATEGY_CACHE.get(type);
        if (strategy == null) {
            strategy = STRATEGIES.stream()
                    .filter(s -> s.canCreate(type, fields))
                    .findFirst()
                    .orElseThrow(() -> new com.paramichha.datafactory.DataFactoryException(
                    com.paramichha.datafactory.ErrorType.NOT_INSTANTIABLE,
                            "No instantiation strategy found for '" + type.getName()
                            + "'. Supported: Lombok @Builder, Record, all-args constructor, no-args constructor + setters."));
            STRATEGY_CACHE.put(type, strategy);
        }
        return strategy.create(type, fields, values);
    }
}
