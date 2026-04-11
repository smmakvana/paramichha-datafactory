package com.paramichha.datafactory.core;

import java.util.List;
import java.util.Map;

/**
 * Assembles an instance of any class from a field-value map by delegating to the first
 * {@link InstantiationStrategy} that can handle it.
 */
public final class ObjectAssembler {

    private static final List<InstantiationStrategy> STRATEGIES = List.of(
            LombokBuilderStrategy.INSTANCE,
            RecordStrategy.INSTANCE,
            AllArgsConstructorStrategy.INSTANCE,
            SetterStrategy.INSTANCE
    );

    private ObjectAssembler() {
    }

    public static <T> T instantiate(Class<T> type, List<FieldDescriptor> fields,
                                    Map<String, Object> values) throws Exception {
        for (InstantiationStrategy strategy : STRATEGIES) {
            if (strategy.canHandle(type, fields)) {
                return strategy.instantiate(type, fields, values);
            }
        }
        throw new IllegalStateException(
                "No instantiation strategy found for " + type.getSimpleName()
                        + ". Implement InstantiationStrategy and add it to ObjectAssembler.");
    }
}
