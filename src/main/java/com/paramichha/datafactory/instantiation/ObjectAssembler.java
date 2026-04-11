package com.paramichha.datafactory.instantiation;

import com.paramichha.datafactory.constraint.FieldDescriptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Assembles an instance of any class from a field-value map by delegating to the first
 * {@link InstantiationStrategy} that can handle it.
 *
 * <p>The winning strategy per class is cached so {@code canHandle()} reflection is only
 * paid once per class — not once per call.
 */
public final class ObjectAssembler {

    private static final List<InstantiationStrategy> STRATEGIES = List.of(
            LombokBuilderStrategy.INSTANCE,
            RecordStrategy.INSTANCE,
            AllArgsConstructorStrategy.INSTANCE,
            SetterStrategy.INSTANCE
    );

    private static final ConcurrentHashMap<Class<?>, InstantiationStrategy> STRATEGY_CACHE
            = new ConcurrentHashMap<>();

    private ObjectAssembler() {
    }

    public static <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                                    Map<String, Object> values) throws Exception {
        InstantiationStrategy strategy = STRATEGY_CACHE.computeIfAbsent(type,
                t -> STRATEGIES.stream()
                        .filter(s -> s.canHandle(t, fields))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "No instantiation strategy found for " + t.getSimpleName()
                                        + ". Implement InstantiationStrategy and add it to ObjectAssembler.")));
        return strategy.instantiate(type, fields, values);
    }
}
